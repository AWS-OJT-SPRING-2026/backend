package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.CreateAssignmentRequest;
import ojt.aws.educare.dto.response.AssignmentDetailResponse;
import ojt.aws.educare.dto.response.AssignmentReportResponse;
import ojt.aws.educare.dto.response.AssignmentResponse;
import ojt.aws.educare.dto.response.QuestionPreviewResponse;
import ojt.aws.educare.entity.Assignment;
import ojt.aws.educare.entity.Answer;
import ojt.aws.educare.entity.Classroom;
import ojt.aws.educare.entity.DisplayAnswerMode;
import ojt.aws.educare.entity.Question;
import ojt.aws.educare.entity.Submission;
import ojt.aws.educare.entity.Teacher;
import ojt.aws.educare.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "assignmentID", ignore = true)
    @Mapping(target = "classroom", source = "classroom")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "teacher", source = "teacher")
    @Mapping(target = "title", source = "request.title")
    @Mapping(target = "assignmentType", source = "assignmentType")
    @Mapping(target = "format", source = "format")
    @Mapping(target = "displayAnswerMode", source = "displayAnswerMode")
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    Assignment toAssignment(
            CreateAssignmentRequest request,
            Classroom classroom,
            User user,
            Teacher teacher,
            String assignmentType,
            String format,
            DisplayAnswerMode displayAnswerMode
    );

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "assignmentType", source = "assignmentType")
    @Mapping(target = "format", source = "format")
    @Mapping(target = "displayAnswerMode", source = "displayAnswerMode")
    void updateResolvedCoreFields(
            @MappingTarget Assignment assignment,
            String title,
            String assignmentType,
            String format,
            DisplayAnswerMode displayAnswerMode
    );

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

    @Mapping(target = "assignmentId", source = "assignment.assignmentID")
    @Mapping(target = "title", source = "assignment.title")
    @Mapping(target = "className", source = "assignment.classroom.className")
    @Mapping(target = "totalStudents", source = "totalStudents")
    @Mapping(target = "totalSubmissions", source = "totalSubmissions")
    @Mapping(target = "completionRate", source = "completionRate")
    @Mapping(target = "passRate", source = "passRate")
    @Mapping(target = "scoreDistribution", source = "scoreDistribution")
    @Mapping(target = "averageScore", source = "averageScore")
    @Mapping(target = "highestScore", source = "highestScore")
    @Mapping(target = "lowestScore", source = "lowestScore")
    @Mapping(target = "studentResults", source = "studentResults")
    @Mapping(target = "questionStats", source = "questionStats")
    @Mapping(target = "questionAnalysis", source = "questionAnalysis")
    AssignmentReportResponse toReportResponse(
            Assignment assignment,
            Integer totalStudents,
            Integer totalSubmissions,
            Double completionRate,
            Double passRate,
            List<Integer> scoreDistribution,
            java.math.BigDecimal averageScore,
            java.math.BigDecimal highestScore,
            java.math.BigDecimal lowestScore,
            List<AssignmentReportResponse.StudentSubmissionSummary> studentResults,
            List<AssignmentReportResponse.QuestionStatistic> questionStats,
            List<AssignmentReportResponse.QuestionAnalysis> questionAnalysis
    );

    @Mapping(target = "submissionId", source = "submission.submissionID")
    @Mapping(target = "userId", source = "submission.user.userID")
    @Mapping(target = "studentName", source = "submission.user.fullName")
    @Mapping(target = "score", source = "submission.score")
    @Mapping(target = "timeTaken", source = "submission.timeTaken")
    @Mapping(target = "submitTime", source = "submission.submittedAt")
    @Mapping(target = "submissionStatus", source = "submissionStatus")
    @Mapping(target = "submissionTimingStatus", source = "submissionTimingStatus")
    @Mapping(target = "violationCount", source = "violationCount")
    AssignmentReportResponse.StudentSubmissionSummary toStudentSubmissionSummary(
            Submission submission,
            String submissionStatus,
            String submissionTimingStatus,
            Integer violationCount
    );

    @Mapping(target = "submissionId", ignore = true)
    @Mapping(target = "userId", source = "user.userID")
    @Mapping(target = "studentName", source = "user.fullName")
    @Mapping(target = "score", source = "score")
    @Mapping(target = "timeTaken", ignore = true)
    @Mapping(target = "submitTime", ignore = true)
    @Mapping(target = "submissionStatus", source = "submissionStatus")
    @Mapping(target = "submissionTimingStatus", source = "submissionTimingStatus")
    @Mapping(target = "violationCount", ignore = true)
    AssignmentReportResponse.StudentSubmissionSummary toMissingStudentSubmissionSummary(
            User user,
            java.math.BigDecimal score,
            String submissionStatus,
            String submissionTimingStatus
    );

    @Mapping(target = "optionId", source = "answer.id")
    @Mapping(target = "optionLabel", source = "answer.label")
    @Mapping(target = "optionContent", source = "answer.content")
    @Mapping(target = "isCorrect", source = "answer.isCorrect")
    @Mapping(target = "selectedCount", source = "selectedCount")
    @Mapping(target = "wrongSelectedCount", source = "wrongSelectedCount")
    AssignmentReportResponse.OptionStatistic toOptionStatistic(
            Answer answer,
            Integer selectedCount,
            Integer wrongSelectedCount
    );

    @Mapping(target = "questionId", source = "questionId")
    @Mapping(target = "questionText", source = "questionText")
    @Mapping(target = "difficultyLevel", source = "difficultyLevel")
    @Mapping(target = "correctCount", source = "correctCount")
    @Mapping(target = "totalAnswered", source = "totalAnswered")
    @Mapping(target = "accuracyRate", source = "accuracyRate")
    @Mapping(target = "options", source = "options")
    AssignmentReportResponse.QuestionAnalysis toQuestionAnalysis(
            Integer questionId,
            String questionText,
            Integer difficultyLevel,
            Integer correctCount,
            Integer totalAnswered,
            Double accuracyRate,
            List<AssignmentReportResponse.OptionStatistic> options
    );

    @Mapping(target = "questionId", source = "questionAnalysis.questionId")
    @Mapping(target = "questionText", source = "questionAnalysis.questionText")
    @Mapping(target = "difficultyLevel", source = "questionAnalysis.difficultyLevel")
    @Mapping(target = "correctCount", source = "questionAnalysis.correctCount")
    @Mapping(target = "totalAnswered", source = "questionAnalysis.totalAnswered")
    @Mapping(target = "accuracyRate", source = "questionAnalysis.accuracyRate")
    AssignmentReportResponse.QuestionStatistic toQuestionStatistic(
            AssignmentReportResponse.QuestionAnalysis questionAnalysis
    );
}
