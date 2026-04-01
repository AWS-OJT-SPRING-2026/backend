package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionPreviewResponse {
    Integer id;
    String questionText;
    String imageUrl;
    String explanation;
    Integer difficultyLevel;
    String difficultyLabel;
    Boolean isAi;
    Integer bankId;
    String bankName;
    List<AnswerResponse> answers;
}
