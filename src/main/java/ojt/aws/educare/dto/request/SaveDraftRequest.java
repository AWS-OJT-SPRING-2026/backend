package ojt.aws.educare.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveDraftRequest {

    @Valid
    @NotNull(message = "QUIZ_DRAFT_QUESTION_ANSWER_REQUIRED")
    List<AnswerItem> answers;

    @NotNull(message = "QUIZ_DRAFT_CURRENT_ANSWER_REQUIRED")
    Integer currentQuestion;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerItem {

        @NotNull(message = "QUIZ_DRAFT_QUESTION_ID_REQUIRED")
        Integer questionId;

        Integer answerRefId;
    }
}
