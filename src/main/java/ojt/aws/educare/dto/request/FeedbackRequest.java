package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackRequest {

    @NotNull(message = "Student ID không được để trống")
    Integer studentId;

    @NotNull(message = "Assignment ID không được để trống")
    Integer assignmentId;

    @NotBlank(message = "Nội dung nhận xét không được để trống")
    String comment;
}
