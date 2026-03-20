package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendanceRequest {
    @NotNull(message = "STUDENT_ID_REQUIRED")
    Integer studentID;

    @NotBlank(message = "STATUS_REQUIRED")
    String status;
    String note;
}