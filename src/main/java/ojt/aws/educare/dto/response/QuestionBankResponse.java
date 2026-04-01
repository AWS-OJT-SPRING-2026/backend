package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionBankResponse {
    Integer id;
    String bankName;
    Integer subjectId;
    String subjectName;
    LocalDateTime createdAt;
}
