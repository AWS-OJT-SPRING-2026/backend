package ojt.aws.educare.service;

import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.NotificationResponse;
import ojt.aws.educare.entity.NotificationType;
import ojt.aws.educare.entity.User;

import java.util.List;

public interface NotificationService {

    ApiResponse<List<NotificationResponse>> getMyNotifications(String category);
    ApiResponse<NotificationResponse> markAsRead(Integer notificationId);
    ApiResponse<Void> markAllAsRead();
    void createNotification(User user, NotificationType type, String title, String content, String actionUrl);
}
