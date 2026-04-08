package ojt.aws.educare.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAssignmentRequest {
    String title;
    String assignmentType;
    String format;
    LocalDateTime startTime;
    LocalDateTime endTime;
    LocalDateTime deadline;
    Integer durationMinutes;
    String displayAnswerMode;
    List<Integer> questionIds;
}
