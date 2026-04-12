package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.FeedbackRequest;
import ojt.aws.educare.dto.response.FeedbackResponse;
import ojt.aws.educare.entity.Assignment;
import ojt.aws.educare.entity.Feedback;
import ojt.aws.educare.entity.Student;
import ojt.aws.educare.entity.Teacher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(target = "feedbackId", ignore = true)
    @Mapping(target = "student", source = "student")
    @Mapping(target = "assignment", source = "assignment")
    @Mapping(target = "teacher", source = "teacher")
    @Mapping(target = "comment", source = "request.comment")
    @Mapping(target = "createdAt", ignore = true)
    Feedback toFeedback(FeedbackRequest request, Student student, Assignment assignment, Teacher teacher);

    @Mapping(target = "studentId", source = "student.studentID")
    @Mapping(target = "studentName", source = "student.fullName")
    @Mapping(target = "assignmentId", source = "assignment.assignmentID")
    @Mapping(target = "assignmentTitle", source = "assignment.title")
    @Mapping(target = "teacherId", source = "teacher.teacherID")
    @Mapping(target = "teacherName", source = "teacher.fullName")
    FeedbackResponse toResponse(Feedback feedback);
}
