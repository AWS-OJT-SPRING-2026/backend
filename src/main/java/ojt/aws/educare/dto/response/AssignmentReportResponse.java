package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentReportResponse {
    Integer assignmentId;
    String title;
    String className;
    Integer totalSubmissions;
    BigDecimal averageScore;
    BigDecimal highestScore;
    BigDecimal lowestScore;
    List<StudentSubmissionSummary> studentResults;
    List<QuestionStatistic> questionStats;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentSubmissionSummary {
        Integer submissionId;
        Integer userId;
        String studentName;
        BigDecimal score;
        Integer timeTaken;
        LocalDateTime submitTime;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionStatistic {
        Integer questionId;
        String questionText;
        Integer difficultyLevel;
        Integer correctCount;
        Integer totalAnswered;
        Double accuracyRate;
    }
}
