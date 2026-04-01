package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAssignmentRequest {
    @NotNull(message = "ClassID không được để trống")
    Integer classroomId;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255)
    String title;

    @NotBlank(message = "Loại bài không được để trống")
    String assignmentType;

    @NotBlank(message = "Hình thức không được để trống")
    String format;

    LocalDateTime startTime;

    LocalDateTime endTime;

    LocalDateTime deadline;

    @NotNull(message = "Thời gian làm bài không được để trống")
    @Positive(message = "Thời gian làm bài phải lớn hơn 0")
    Integer durationMinutes;

    @NotEmpty(message = "Phải có ít nhất 1 câu hỏi")
    List<Integer> questionIds;
}
