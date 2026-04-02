package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.WeeklyGradeDayResponse;
import ojt.aws.educare.repository.projection.WeeklyGradeAggregationProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WeeklyGradeStatsMapper {

    @Mapping(target = "dayLabel", ignore = true)
    WeeklyGradeDayResponse toWeeklyGradeDayResponse(WeeklyGradeAggregationProjection projection);
}

