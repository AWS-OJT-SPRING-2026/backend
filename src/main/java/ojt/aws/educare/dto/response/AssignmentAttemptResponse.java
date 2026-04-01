package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignmentAttemptResponse {
    Integer submissionID;
    Integer assignmentId;
    LocalDateTime startedAt;
    LocalDateTime expiredAt;
}

