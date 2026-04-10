package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.configuration.CurrentUserProvider;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.NotificationResponse;
import ojt.aws.educare.entity.Notification;
import ojt.aws.educare.entity.NotificationType;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.NotificationMapper;
import ojt.aws.educare.repository.NotificationRepository;
import ojt.aws.educare.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationServiceImpl implements NotificationService {

    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;
    CurrentUserProvider currentUserProvider;

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

        List<NotificationResponse> result = notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());

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
        return ApiResponse.<NotificationResponse>builder()
                .result(notificationMapper.toResponse(notification))
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
    public void createNotification(User user, NotificationType type, String title, String content, String actionUrl) {
        // Prevent duplicates: skip if identical notification already exists
        if (actionUrl != null && notificationRepository.existsByUser_UserIDAndTypeAndActionUrl(
                user.getUserID(), type, actionUrl)) {
            return;
        }
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .actionUrl(actionUrl)
                .isRead(Boolean.FALSE)
                .build();
        notificationRepository.save(notification);
    }
}
