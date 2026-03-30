package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookHierarchyResponse {
    Integer id;
    String bookName;
    Integer subjectId;
    String subjectName;
    List<ChapterResponse> chapters;
}
