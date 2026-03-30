package ojt.aws.educare.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentDistributionUpdateRequest {
    String type; // THEORY | QUESTION
    List<Integer> classIds;
}

