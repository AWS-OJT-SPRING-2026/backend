package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimetableRequest {
    @NotNull(message = "CLASS_ID_REQUIRED")
    Integer classID;

    Integer teacherID;
    String topic;
    String googleMeetLink;

    @NotNull(message = "START_TIME_REQUIRED")
    LocalDateTime startTime;

    @NotNull(message = "END_TIME_REQUIRED")
    LocalDateTime endTime;
}