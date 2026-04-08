package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentDetailResponse {
    Integer assignmentID;
    String title;
    String assignmentType;
    String format;
    String status;
    LocalDateTime startTime;
    LocalDateTime endTime;
    LocalDateTime deadline;
    Integer durationMinutes;
    String displayAnswerMode;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Integer classroomId;
    String className;
    String subjectName;
    Integer totalQuestions;
    Integer totalSubmissions;
    List<QuestionPreviewResponse> questions;
}
