package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.NotificationResponse;
import ojt.aws.educare.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "assignmentDeadline", ignore = true)
    @Mapping(target = "testStartTime", ignore = true)
    NotificationResponse toResponse(Notification notification);
}
