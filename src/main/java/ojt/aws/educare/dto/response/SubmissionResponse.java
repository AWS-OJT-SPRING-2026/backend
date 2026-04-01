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
public class SubmissionResponse {
    Integer submissionID;
    Integer assignmentId;
    String assignmentTitle;
    Integer userId;
    String studentName;
    BigDecimal score;
    Integer timeTaken;
    LocalDateTime startedAt;
    LocalDateTime expiredAt;
    LocalDateTime submittedAt;
    LocalDateTime submitTime;
    List<SubmissionAnswerDetail> answers;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubmissionAnswerDetail {
        Integer questionId;
        String questionText;
        Integer answerRefId;
        String selectedAnswer;
        Boolean isCorrect;
    }
}
