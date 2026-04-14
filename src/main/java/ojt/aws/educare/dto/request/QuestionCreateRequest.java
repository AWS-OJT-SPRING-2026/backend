package ojt.aws.educare.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionCreateRequest {

    @NotBlank(message = "QUESTION_TEXT_REQUIRED")
    String questionText;

    String imageUrl;
    String explanation;

    @NotNull(message = "DIFFICULTY_LEVEL_REQUIRED")
    Integer difficultyLevel;

    @NotEmpty(message = "ANSWERS_REQUIRED")
    @Valid
    List<AnswerRequest> answers;
}
