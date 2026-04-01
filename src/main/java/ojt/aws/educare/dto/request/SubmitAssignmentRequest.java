package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitAssignmentRequest {
    @NotNull
    Integer assignmentId;

    @NotNull
    Integer timeTaken;

    @NotEmpty
    List<AnswerItem> answers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnswerItem {
        Integer questionId;
        Integer answerRefId;
        String selectedAnswer;
    }
}
