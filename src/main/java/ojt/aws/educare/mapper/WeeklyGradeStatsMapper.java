package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.WeeklyGradeDayResponse;
import ojt.aws.educare.repository.projection.WeeklyGradeAggregationProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WeeklyGradeStatsMapper {

    @Mapping(target = "dayLabel", ignore = true)
    WeeklyGradeDayResponse toWeeklyGradeDayResponse(WeeklyGradeAggregationProjection projection);

    default WeeklyGradeDayResponse toDefaultWeeklyGradeDayResponse(Integer dayOfWeek, String dayLabel) {
        return WeeklyGradeDayResponse.builder()
                .dayOfWeek(dayOfWeek)
                .dayLabel(dayLabel)
                .hocSinhGioiKha(0L)
                .hocSinhYeuKem(0L)
                .tongBaiCham(0L)
                .build();
    }
}

