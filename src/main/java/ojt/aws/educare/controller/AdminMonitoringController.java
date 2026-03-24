package ojt.aws.educare.controller;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.entity.Violation;
import ojt.aws.educare.repository.UserRepository;
import ojt.aws.educare.repository.ViolationRepository;
import ojt.aws.educare.service.MonitoringService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminMonitoringController {
    MonitoringService monitoringService;
    ViolationRepository violationRepository;
    UserRepository userRepository;

    @Data
    @Builder
    public static class ActiveUserResponse {
        Integer userID;
        String username;
        String fullName;
        String roleName;
        String deviceInfo;
        String ipAddress;
        LocalDateTime lastActiveAt;
        int recentActivityCount;
        int activeDeviceCount;
        String status;
    }

    @GetMapping("/monitoring/active-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ActiveUserResponse>> getActiveUsers() {
        List<ActiveUserResponse> activeUsers = monitoringService.getActiveUserIds().stream().map(username -> {
            User user = userRepository.findByUsernameIgnoreCase(username).orElse(null);
            return ActiveUserResponse.builder()
                    .userID(user != null ? user.getUserID() : 0)
                    .username(username)
                    .fullName(user != null ? user.getFullName() : username)
                    .roleName(user != null && user.getRole() != null ? user.getRole().getRoleName() : "USER")
                    .deviceInfo("Web Browser")
                    .ipAddress("127.0.0.1")
                    .lastActiveAt(LocalDateTime.now())
                    .recentActivityCount((int) monitoringService.getRecentActivityCount(username))
                    .activeDeviceCount(monitoringService.getDeviceCount(username))
                    .status("ACTIVE")
                    .build();
        }).collect(Collectors.toList());

        return ApiResponse.<List<ActiveUserResponse>>builder()
                .result(activeUsers)
                .build();
    }

    @GetMapping("/monitoring/my-status")
    public ApiResponse<Map<String, Object>> getMyStatus() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
             return ApiResponse.<Map<String, Object>>builder().result(Map.of("locked", false)).build();
        }
        String username = auth.getName();
        boolean isLocked = monitoringService.isUserLocked(username);
        return ApiResponse.<Map<String, Object>>builder()
                .result(Map.of("locked", isLocked, "username", username))
                .build();
    }

    @GetMapping("/violations")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Violation>> getViolations() {
        return ApiResponse.<List<Violation>>builder()
                .result(violationRepository.findAllByOrderByCreatedAtDesc())
                .build();
    }

    @PostMapping("/monitoring/kick/{userID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> kickUser(@PathVariable Integer userID) {
        User user = userRepository.findById(userID).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != null && "ADMIN".equals(user.getRole().getRoleName())) {
            throw new RuntimeException("Cannot kick an admin");
        }
        monitoringService.lockUser(user.getUsername(), 5); // Lock for 5 mins as "kick"
        monitoringService.createViolation(user.getUsername(), "MANUAL_KICK", "Bị Admin kick thủ công", "WARN_KICK");
        return ApiResponse.<Void>builder().message("User kicked").build();
    }

    @DeleteMapping("/violations/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteViolation(@PathVariable Integer id) {
        violationRepository.deleteById(id);
        return ApiResponse.<Void>builder().message("Violation deleted").build();
    }

    @PostMapping("/violations/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resolveViolation(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        Violation v = violationRepository.findById(id).orElseThrow(() -> new RuntimeException("Violation not found"));
        v.setResolved(true);
        if ("UNBAN".equals(body.get("action"))) {
            monitoringService.lockUser(v.getUsername(), 0); // Now removes the lock
        } else if ("PERMANENT_BAN".equals(body.get("action"))) {
            monitoringService.lockUser(v.getUsername(), 999999);
        }
        violationRepository.save(v);
        return ApiResponse.<Void>builder().message("Violation resolved").build();
    }
}
