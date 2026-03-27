package ojt.aws.educare.entity;

import java.io.Serializable;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionContentBlockId implements Serializable {
    private Integer questionId;
    private Integer contentBlockId;
}
