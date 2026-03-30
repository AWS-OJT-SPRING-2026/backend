package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentTheorySubjectOverviewResponse {
    Integer subjectId;
    String subjectName;
    Integer bookId;
    Long totalChapters;
    Long totalLessons;
}
