package ojt.aws.educare.dto.response;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimetableStatsResponse {
    long totalToday;
    long ongoing;
    long upcoming;
    long completed;
}