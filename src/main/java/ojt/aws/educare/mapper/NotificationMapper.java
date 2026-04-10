package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.NotificationResponse;
import ojt.aws.educare.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}
