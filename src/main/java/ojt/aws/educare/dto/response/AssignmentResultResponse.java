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
public class AssignmentResultResponse {
    Integer submissionId;
    Integer assignmentId;
    String assignmentTitle;
    Integer userId;
    String studentName;
    BigDecimal score;
    Integer totalQuestions;
    Integer correctCount;
    Integer timeTaken;
    LocalDateTime submitTime;
    String submissionStatus;
    String displayAnswerMode;
    Boolean canViewResult;
    Boolean canViewDetailedAnswers;
    LocalDateTime revealAt;
    String visibilityMessage;
    List<QuestionResult> questions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionResult {
        Integer questionId;
        String questionText;
        Integer selectedAnswerRefId;
        String selectedAnswer;
        Integer correctAnswerRefId;
        String correctAnswer;
        Boolean isCorrect;
    }
}

