package ojt.aws.educare.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizDraftResponse {

    Integer assignmentId;
    Integer submissionId;
    List<SavedAnswer> answers;
    Integer currentQuestion;

    LocalDateTime lastSavedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavedAnswer {
        Integer questionId;
        Integer answerRefId;
    }
}
