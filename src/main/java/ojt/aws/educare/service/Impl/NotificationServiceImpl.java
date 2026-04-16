package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.configuration.CurrentUserProvider;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.NotificationResponse;
import ojt.aws.educare.entity.Assignment;
import ojt.aws.educare.entity.Classroom;
import ojt.aws.educare.entity.ClassMember;
import ojt.aws.educare.entity.Notification;
import ojt.aws.educare.entity.NotificationType;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.NotificationMapper;
import ojt.aws.educare.repository.AssignmentRepository;
import ojt.aws.educare.repository.ClassroomRepository;
import ojt.aws.educare.repository.ClassMemberRepository;
import ojt.aws.educare.repository.NotificationRepository;
import ojt.aws.educare.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationServiceImpl implements NotificationService {

    private static final int RETENTION_DAYS = 30;
    private static final int DUPLICATE_WINDOW_SECONDS = 30;

    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;
    CurrentUserProvider currentUserProvider;
    AssignmentRepository assignmentRepository;
    ClassMemberRepository classMemberRepository;
    ClassroomRepository classroomRepository;

    static final String ASSIGNMENT_TYPE_TEST = "TEST";
    static final Pattern TEST_ACTION_URL_PATTERN = Pattern.compile("^/student/tests/(\\d+)$");

    static final Set<NotificationType> ASSIGNMENT_CONTEXT_TYPES = EnumSet.of(
            NotificationType.ASSIGNMENT_NEW,
            NotificationType.ASSIGNMENT_DUE_SOON,
            NotificationType.ASSIGNMENT_OVERDUE,
            NotificationType.TEST_UPCOMING,
            NotificationType.TEST_STARTING
    );

    private static final List<NotificationType> IMPORTANT_TYPES = Arrays.asList(
            NotificationType.ASSIGNMENT_DUE_SOON,
            NotificationType.ASSIGNMENT_OVERDUE,
            NotificationType.TEST_STARTING
    );

    private static final List<NotificationType> LEARNING_TYPES = Arrays.asList(
            NotificationType.ASSIGNMENT_NEW,
            NotificationType.TEST_UPCOMING,
            NotificationType.TEST_RESULT,
            NotificationType.FEEDBACK_RECEIVED
    );

    private static final List<NotificationType> SYSTEM_TYPES = Arrays.asList(
            NotificationType.SCHEDULE_CHANGED,
            NotificationType.TEACHER_CHANGED,
            NotificationType.REMINDER_CLASS
    );

    @Override
    public ApiResponse<List<NotificationResponse>> getMyNotifications(String category) {
        User currentUser = currentUserProvider.getCurrentUser();
        List<Notification> notifications;

        if (category == null || category.isBlank() || category.equalsIgnoreCase("ALL")) {
            notifications = notificationRepository.findByUser_UserIDOrderByCreatedAtDesc(currentUser.getUserID());
        } else if (category.equalsIgnoreCase("IMPORTANT")) {
            notifications = notificationRepository.findByUserIdAndTypeIn(currentUser.getUserID(), IMPORTANT_TYPES);
        } else if (category.equalsIgnoreCase("LEARNING")) {
            notifications = notificationRepository.findByUserIdAndTypeIn(currentUser.getUserID(), LEARNING_TYPES);
        } else if (category.equalsIgnoreCase("SYSTEM")) {
            notifications = notificationRepository.findByUserIdAndTypeIn(currentUser.getUserID(), SYSTEM_TYPES);
        } else {
            notifications = notificationRepository.findByUser_UserIDOrderByCreatedAtDesc(currentUser.getUserID());
        }

        Map<Integer, Assignment> assignmentById = buildAssignmentMap(notifications);
        List<NotificationResponse> result = notifications.stream()
                .map(notification -> toResponse(notification, assignmentById))
                .toList();

        return ApiResponse.<List<NotificationResponse>>builder().result(result).build();
    }

    @Override
    @Transactional
    public ApiResponse<NotificationResponse> markAsRead(Integer notificationId) {
        User currentUser = currentUserProvider.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getUserID().equals(currentUser.getUserID())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        notification.setIsRead(Boolean.TRUE);
        notificationRepository.save(notification);
        Map<Integer, Assignment> assignmentById = buildAssignmentMap(List.of(notification));
        return ApiResponse.<NotificationResponse>builder()
                .result(toResponse(notification, assignmentById))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> markAllAsRead() {
        User currentUser = currentUserProvider.getCurrentUser();
        notificationRepository.markAllReadByUserId(currentUser.getUserID());
        return ApiResponse.<Void>builder()
                .message("Đã đánh dấu tất cả thông báo là đã đọc")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteMyNotification(Integer notificationId) {
        User currentUser = currentUserProvider.getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getUserID().equals(currentUser.getUserID())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        notificationRepository.delete(notification);

        return ApiResponse.<Void>builder().message("Đã xóa thông báo").build();
    }
    @Override
    @Transactional
    public void createNotification(User user, NotificationType type, String title, String content, String actionUrl) {
        // Keep duplicate guard for most types, but allow repeated class/schedule updates.
        if (!shouldBypassDuplicateGuard(type)
                && actionUrl != null
                && notificationRepository.existsByUser_UserIDAndTypeAndActionUrl(
                user.getUserID(), type, actionUrl)) {
            return;
        }

        // For schedule/teacher updates: only suppress exact same message in a short time window.
        if (shouldBypassDuplicateGuard(type)
                && actionUrl != null
                && content != null
                && notificationRepository.existsByUser_UserIDAndTypeAndActionUrlAndContentAndCreatedAtAfter(
                user.getUserID(),
                type,
                actionUrl,
                content,
                LocalDateTime.now().minusSeconds(DUPLICATE_WINDOW_SECONDS))) {
            return;
        }

        Notification notification = notificationMapper.toNotification(user, type, title, content, actionUrl);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void notifyClassroomStudents(Integer classId, NotificationType type, String title, String content, String actionUrl) {
        List<ClassMember> members = classMemberRepository.findByClassroomClassID(classId);
        for (ClassMember member : members) {
            if (member.getStudent() == null || member.getStudent().getUser() == null) {
                continue;
            }
            createNotification(member.getStudent().getUser(), type, title, content, actionUrl);
        }
    }

    @Override
    @Transactional
    public void notifyClassroomParticipants(Integer classId, NotificationType type, String title, String content, String actionUrl) {
        classroomRepository.findById(classId)
                .map(Classroom::getTeacher)
                .map(teacher -> teacher.getUser())
                .ifPresent(user -> createNotification(user, type, title, content, actionUrl));

        notifyClassroomStudents(classId, type, title, content, actionUrl);
    }

    @Override
    @Transactional
    public void deleteByActionUrl(String actionUrl) {
        if (actionUrl == null || actionUrl.isBlank()) {
            return;
        }
        notificationRepository.deleteByActionUrl(actionUrl);
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeExpiredNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
        notificationRepository.deleteByCreatedAtBefore(cutoff);
    }

    private boolean shouldBypassDuplicateGuard(NotificationType type) {
        return type == NotificationType.SCHEDULE_CHANGED || type == NotificationType.TEACHER_CHANGED;
    }

    private NotificationResponse toResponse(Notification notification, Map<Integer, Assignment> assignmentById) {
        NotificationResponse response = notificationMapper.toResponse(notification);
        resolveAssignment(notification, assignmentById).ifPresent(assignment -> {
            if (notification.getType() == NotificationType.ASSIGNMENT_NEW) {
                if (ASSIGNMENT_TYPE_TEST.equalsIgnoreCase(assignment.getAssignmentType())) {
                    response.setTestStartTime(assignment.getStartTime());
                } else {
                    response.setAssignmentDeadline(assignment.getDeadline());
                }
                return;
            }

            if (notification.getType() == NotificationType.TEST_UPCOMING) {
                response.setTitle("Bài kiểm tra sắp diễn ra");
                response.setContent(buildUpcomingTestContent(assignment));
                response.setTestStartTime(assignment.getStartTime());
                return;
            }

            if (notification.getType() == NotificationType.TEST_STARTING) {
                response.setTestStartTime(assignment.getStartTime());
                return;
            }

            if (notification.getType() == NotificationType.ASSIGNMENT_DUE_SOON
                    || notification.getType() == NotificationType.ASSIGNMENT_OVERDUE) {
                response.setAssignmentDeadline(assignment.getDeadline());
            }
        });
        return response;
    }

    private String buildUpcomingTestContent(Assignment assignment) {
        LocalDateTime startTime = assignment.getStartTime();
        if (startTime == null) {
            return "Bài kiểm tra \"" + assignment.getTitle() + "\" sẽ sớm bắt đầu";
        }

        LocalDateTime now = LocalDateTime.now();
        if (startTime.toLocalDate().isEqual(LocalDate.now().plusDays(1))) {
            return "Bài kiểm tra \"" + assignment.getTitle() + "\" sẽ bắt đầu vào ngày mai";
        }

        long minutes = Duration.between(now, startTime).toMinutes();
        if (minutes > 0 && minutes <= 60) {
            return "Bài kiểm tra \"" + assignment.getTitle() + "\" sẽ bắt đầu sau " + minutes + " phút";
        }

        if (minutes > 60) {
            long hours = Math.round(minutes / 60.0);
            return "Bài kiểm tra \"" + assignment.getTitle() + "\" sẽ bắt đầu sau " + hours + " giờ";
        }

        return "Bài kiểm tra \"" + assignment.getTitle() + "\" đang bắt đầu";
    }

    private Map<Integer, Assignment> buildAssignmentMap(List<Notification> notifications) {
        Map<Integer, Assignment> assignmentById = new HashMap<>();
        List<Integer> assignmentIds = notifications.stream()
                .filter(notification -> ASSIGNMENT_CONTEXT_TYPES.contains(notification.getType()))
                .map(Notification::getActionUrl)
                .map(this::extractAssignmentId)
                .flatMap(Optional::stream)
                .distinct()
                .toList();

        if (assignmentIds.isEmpty()) {
            return assignmentById;
        }

        assignmentRepository.findAllById(assignmentIds)
                .forEach(assignment -> assignmentById.put(assignment.getAssignmentID(), assignment));
        return assignmentById;
    }

    private Optional<Assignment> resolveAssignment(Notification notification, Map<Integer, Assignment> assignmentById) {
        if (notification.getActionUrl() == null || !ASSIGNMENT_CONTEXT_TYPES.contains(notification.getType())) {
            return Optional.empty();
        }

        return extractAssignmentId(notification.getActionUrl())
                .map(assignmentById::get);
    }

    private Optional<Integer> extractAssignmentId(String actionUrl) {
        if (actionUrl == null) {
            return Optional.empty();
        }

        Matcher matcher = TEST_ACTION_URL_PATTERN.matcher(actionUrl);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(matcher.group(1)));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
