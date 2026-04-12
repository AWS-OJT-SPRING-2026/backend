package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.NotificationResponse;
import ojt.aws.educare.entity.Notification;
import ojt.aws.educare.entity.NotificationType;
import ojt.aws.educare.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "notificationId", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "actionUrl", source = "actionUrl")
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    Notification toNotification(User user, NotificationType type, String title, String content, String actionUrl);

    @Mapping(target = "assignmentDeadline", ignore = true)
    @Mapping(target = "testStartTime", ignore = true)
    NotificationResponse toResponse(Notification notification);
}
