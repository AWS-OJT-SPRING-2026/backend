package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitAssignmentRequest {
    @NotNull(message = "INVALID_KEY")
    @PositiveOrZero(message = "INVALID_KEY")
    Integer timeTaken;

    @Valid
    @NotEmpty(message = "INVALID_KEY")
    List<AnswerItem> answers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnswerItem {
        @NotNull(message = "INVALID_KEY")
        Integer questionId;
        Integer answerRefId;
        String selectedAnswer;
    }
}
