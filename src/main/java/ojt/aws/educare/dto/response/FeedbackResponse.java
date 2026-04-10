package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponse {
    Integer feedbackId;
    Integer studentId;
    String studentName;
    Integer assignmentId;
    String assignmentTitle;
    Integer teacherId;
    String teacherName;
    String comment;
    LocalDateTime createdAt;
}
