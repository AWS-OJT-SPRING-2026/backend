package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendNotificationRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    String title;

    String content;

    String actionUrl;
}
