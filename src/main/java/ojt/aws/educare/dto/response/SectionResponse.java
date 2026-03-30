package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SectionResponse {
    Integer id;
    String sectionNumber;
    String sectionTitle;
    List<SubsectionResponse> subsections;
}


