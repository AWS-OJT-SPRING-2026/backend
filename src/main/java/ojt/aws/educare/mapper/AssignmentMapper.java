package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.AssignmentDetailResponse;
import ojt.aws.educare.dto.response.AssignmentResponse;
import ojt.aws.educare.dto.response.QuestionPreviewResponse;
import ojt.aws.educare.entity.Assignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    default AssignmentResponse toResponse(Assignment assignment) {
        return toResponse(assignment, 0, 0);
    }

    @Mapping(target = "assignmentID", source = "assignment.assignmentID")
    @Mapping(target = "classroomId", source = "assignment.classroom.classID")
    @Mapping(target = "className", source = "assignment.classroom.className")
    @Mapping(target = "subjectName", source = "assignment.classroom.subject.subjectName")
    @Mapping(target = "totalQuestions", source = "totalQuestions")
    @Mapping(target = "totalSubmissions", source = "totalSubmissions")
    AssignmentResponse toResponse(Assignment assignment, int totalQuestions, int totalSubmissions);

    default AssignmentDetailResponse toDetailResponse(Assignment assignment, List<QuestionPreviewResponse> questions) {
        return toDetailResponse(assignment, questions, 0);
    }

    @Mapping(target = "assignmentID", source = "assignment.assignmentID")
    @Mapping(target = "classroomId", source = "assignment.classroom.classID")
    @Mapping(target = "className", source = "assignment.classroom.className")
    @Mapping(target = "subjectName", source = "assignment.classroom.subject.subjectName")
    @Mapping(target = "totalQuestions", expression = "java(questions != null ? questions.size() : 0)")
    @Mapping(target = "totalSubmissions", source = "totalSubmissions")
    @Mapping(target = "questions", source = "questions")
    AssignmentDetailResponse toDetailResponse(
            Assignment assignment,
            List<QuestionPreviewResponse> questions,
            int totalSubmissions
    );
}
