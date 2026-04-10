package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.entity.NotificationType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Integer notificationId;
    NotificationType type;
    String title;
    String content;
    Boolean isRead;
    String actionUrl;
    LocalDateTime createdAt;
}
