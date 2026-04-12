package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.UpcomingTaskResponse;
import ojt.aws.educare.entity.Assignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UpcomingTaskMapper {

    @Mapping(target = "id", source = "assignmentID")
    @Mapping(target = "type", source = "assignmentType")
    @Mapping(target = "deadline", expression = "java(isTest(assignment) ? null : assignment.getDeadline())")
    @Mapping(target = "startTime", expression = "java(isTest(assignment) ? assignment.getStartTime() : null)")
    @Mapping(target = "progress", ignore = true)
    @Mapping(target = "actionUrl", expression = "java(\"/student/tests/\" + assignment.getAssignmentID())")
    UpcomingTaskResponse toResponse(Assignment assignment);

    @Named("isTest")
    default boolean isTest(Assignment assignment) {
        return "TEST".equals(assignment.getAssignmentType());
    }
}
