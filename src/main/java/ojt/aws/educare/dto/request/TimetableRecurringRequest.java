package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimetableRecurringRequest {
    @NotNull(message = "CLASS_ID_REQUIRED")
    Integer classID;

    Integer teacherID;
    String topic;
    String googleMeetLink;

    @NotNull(message = "START_DATE_REQUIRED")
    LocalDate startDate;

    @NotNull(message = "END_DATE_REQUIRED")
    LocalDate endDate;

    @NotNull(message = "START_TIME_REQUIRED")
    LocalTime startTime;

    @NotNull(message = "END_TIME_REQUIRED")
    LocalTime endTime;

    @NotEmpty(message = "DAYS_OF_WEEK_REQUIRED")
    List<DayOfWeek> daysOfWeek;
}