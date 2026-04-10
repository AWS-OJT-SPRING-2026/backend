package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.FeedbackResponse;
import ojt.aws.educare.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(target = "studentId", source = "student.studentID")
    @Mapping(target = "studentName", source = "student.fullName")
    @Mapping(target = "assignmentId", source = "assignment.assignmentID")
    @Mapping(target = "assignmentTitle", source = "assignment.title")
    @Mapping(target = "teacherId", source = "teacher.teacherID")
    @Mapping(target = "teacherName", source = "teacher.fullName")
    FeedbackResponse toResponse(Feedback feedback);
}
