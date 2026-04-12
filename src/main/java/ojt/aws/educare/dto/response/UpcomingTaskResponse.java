package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpcomingTaskResponse {
    Long id;
    String type;          // "ASSIGNMENT" or "TEST"
    String title;
    LocalDateTime deadline;   // for assignments
    LocalDateTime startTime;  // for tests
    Integer progress;         // 0–100 (optional)
    String actionUrl;
}
