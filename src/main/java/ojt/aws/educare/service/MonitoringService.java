package ojt.aws.educare.service;

import ojt.aws.educare.entity.User;
import ojt.aws.educare.entity.UserActivity;
import ojt.aws.educare.entity.Violation;
import ojt.aws.educare.repository.UserActivityRepository;
import ojt.aws.educare.repository.UserRepository;
import ojt.aws.educare.repository.ViolationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MonitoringService {
    @Autowired
    private UserActivityRepository userActivityRepository;
    @Autowired
    private ViolationRepository violationRepository;
    @Autowired
    private UserRepository userRepository;

    // Map: username -> (sessionKey -> lastActiveAt)
    // sessionKey = IP + "|" + User-Agent
    private final Map<String, Map<String, LocalDateTime>> activeUserSessions = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lockedUsers = new ConcurrentHashMap<>();

    // Record user activity and update active status
    public void trackActivity(String userIdOrUsername, String action, String details, String ip, String userAgent) {
        if (userIdOrUsername == null) return;
        String userKey = userIdOrUsername.toLowerCase();

        if (isUserLocked(userKey)) {
            return;
        }

        UserActivity activity = UserActivity.builder()
                .userId(userIdOrUsername)
                .action(action)
                .details(details)
                .ipAddress(ip)
                .build();
        userActivityRepository.save(activity);

        String sessionKey = ip + "|" + (userAgent != null ? userAgent : "Unknown");
        activeUserSessions.computeIfAbsent(userKey, k -> new ConcurrentHashMap<>())
                .put(sessionKey, LocalDateTime.now());

        // Spam detection
        if (isSpamming(userKey, action)) {
            lockUser(userKey, 1); // Kick for 1 minute
            createViolation(userKey, "SPAM_DETECTED", "Phát hiện spam liên tục cho hành động: " + action, "WARN_KICK");
        }

        // Suspicious score edit detection
        if ("EDIT_SCORE".equals(action)) {
            long edits = userActivityRepository.countByUserIdAndActionAndTimestampAfter(
                    userIdOrUsername, "EDIT_SCORE", LocalDateTime.now().minusMinutes(10));
            if (edits >= 5) {
                lockUser(userKey, 30); // Lock for 30 minutes
                createViolation(userKey, "SUSPICIOUS_SCORE_EDIT", "Sửa điểm " + edits + " lần trong 10 phút.", "TEMP_BAN_30M");
            }
        }
    }

    private boolean isSpamming(String userId, String action) {
        LocalDateTime since = LocalDateTime.now().minusSeconds(10);
        long count = userActivityRepository.countByUserIdAndActionAndTimestampAfter(userId, action, since);
        return count >= 10;
    }

    public void removeActiveUser(String username) {
        if (username != null) activeUserSessions.remove(username.toLowerCase());
    }

    public void createViolation(String username, String type, String desc, String actionType) {
        if (username == null) return;
        String userKey = username.toLowerCase();

        // Prevent duplicate manual kicks if already locked
        if ("MANUAL_KICK".equals(type) && isUserLocked(userKey)) {
            return;
        }

        User user = userRepository.findByUsernameIgnoreCase(username).orElse(null);

        String fullName = (user != null) ? user.getFullName() : username;
        String role = (user != null && user.getRole() != null) ? user.getRole().getRoleName() : "UNKNOWN";

        Violation violation = Violation.builder()
                .username(user != null ? user.getUsername() : userKey)
                .fullName(fullName)
                .roleName(role)
                .violationType(type)
                .description(desc)
                .action(actionType)
                .bannedUntil(LocalDateTime.now().plusMinutes("TEMP_BAN_30M".equals(actionType) ? 30 : 5))
                .isResolved(false)
                .build();
        violationRepository.save(violation);
        
        System.out.println("VIOLATION created: " + type + " for user: " + userKey);
    }

    public boolean isUserLocked(String userId) {
        if (userId == null) return false;
        String userKey = userId.toLowerCase();
        LocalDateTime lockUntil = lockedUsers.get(userKey);
        
        if (lockUntil != null && lockUntil.isAfter(LocalDateTime.now())) {
            return true;
        }
        
        if (lockUntil != null) {
            lockedUsers.remove(userKey);
        }
        return false;
    }

    public void lockUser(String userId, int minutes) {
        if (userId == null) return;
        String userKey = userId.toLowerCase();

        if (minutes <= 0) {
            lockedUsers.remove(userKey);
            System.out.println("UNLOCKing user: " + userKey);
            return;
        }
        
        // Remove from active list immediately when kicking
        removeActiveUser(userKey);
        
        lockedUsers.put(userKey, LocalDateTime.now().plusMinutes(minutes));
        System.out.println("KICK/LOCK enforced for user: " + userKey + " for " + minutes + " mins");
    }

    public int getActiveUserCount() {
        return activeUserSessions.size();
    }

    public List<String> getActiveUserIds() {
        return activeUserSessions.keySet().stream().collect(Collectors.toList());
    }
    
    public int getDeviceCount(String username) {
        if (username == null) return 0;
        Map<String, LocalDateTime> sessions = activeUserSessions.get(username.toLowerCase());
        return sessions != null ? sessions.size() : 0;
    }

    public Map<String, LocalDateTime> getLockedUsers() {
        return Collections.unmodifiableMap(lockedUsers);
    }
    
    public long getRecentActivityCount(String username) {
        return userActivityRepository.countByUserIdAndActionAndTimestampAfter(
                username, null, LocalDateTime.now().minusSeconds(30)); 
    }

    @Scheduled(fixedDelay = 60000)
    public void cleanupActiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        activeUserSessions.forEach((user, sessions) -> {
            sessions.entrySet().removeIf(session -> session.getValue().isBefore(threshold));
        });
        activeUserSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
