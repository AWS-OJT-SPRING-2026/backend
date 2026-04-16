package ojt.aws.educare.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.NotificationResponse;
import ojt.aws.educare.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ojt.aws.educare.dto.request.SendNotificationRequest;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ApiResponse<List<NotificationResponse>> getMyNotifications(
            @RequestParam(required = false) String category) {
        return notificationService.getMyNotifications(category);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable Integer id) {
        return notificationService.markAsRead(id);
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ApiResponse<Void> markAllAsRead() {
        return notificationService.markAllAsRead();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ApiResponse<Void> deleteMyNotification(@PathVariable Integer id) {
        return notificationService.deleteMyNotification(id);
    }

    @PostMapping("/send-class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> sendClassNotification(
            @PathVariable Integer classId,
            @RequestBody @Valid SendNotificationRequest request) {
        return notificationService.sendClassNotification(classId, request);
    }
}
