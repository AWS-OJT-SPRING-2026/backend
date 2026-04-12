package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.WeeklyProgressResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WeeklyProgressMapper {

    default WeeklyProgressResponse.Breakdown toBreakdown(
            int assignmentDone,
            int assignmentTotal,
            int testDone,
            int testTotal,
            int attendanceDone,
            int attendanceTotal
    ) {
        return WeeklyProgressResponse.Breakdown.builder()
                .assignmentDone(assignmentDone)
                .assignmentTotal(assignmentTotal)
                .testDone(testDone)
                .testTotal(testTotal)
                .attendanceDone(attendanceDone)
                .attendanceTotal(attendanceTotal)
                .build();
    }

    default WeeklyProgressResponse toResponse(int progressPercent, int totalTasks, int completedTasks, WeeklyProgressResponse.Breakdown breakdown) {
        return WeeklyProgressResponse.builder()
                .progressPercent(progressPercent)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .breakdown(breakdown)
                .build();
    }
}


