package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerRequest {
    Integer id; // Can be null when creating, needed for mapping updates

    @NotBlank(message = "ANSWER_LABEL_REQUIRED")
    String label; // A, B, C, D

    @NotBlank(message = "ANSWER_CONTENT_REQUIRED")
    String content;

    @NotNull(message = "IS_CORRECT_REQUIRED")
    Boolean isCorrect;
}
