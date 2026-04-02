package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeeklyGradeDayResponse {
    Integer dayOfWeek;
    String dayLabel;
    Long hocSinhGioiKha;
    Long hocSinhYeuKem;
    Long tongBaiCham;
}

