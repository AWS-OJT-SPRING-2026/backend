package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.SendNotificationRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.NotificationResponse;
import ojt.aws.educare.entity.NotificationType;
import ojt.aws.educare.entity.User;

import java.util.List;

public interface NotificationService {

    ApiResponse<List<NotificationResponse>> getMyNotifications(String category);
    ApiResponse<NotificationResponse> markAsRead(Integer notificationId);
    ApiResponse<Void> markAllAsRead();
    ApiResponse<Void> deleteMyNotification(Integer notificationId);
    void createNotification(User user, NotificationType type, String title, String content, String actionUrl);
    void notifyClassroomStudents(Integer classId, NotificationType type, String title, String content, String actionUrl);
    void notifyClassroomParticipants(Integer classId, NotificationType type, String title, String content, String actionUrl);
    void deleteByActionUrl(String actionUrl);
    ApiResponse<Void> sendClassNotification(Integer classId, SendNotificationRequest request);
}
