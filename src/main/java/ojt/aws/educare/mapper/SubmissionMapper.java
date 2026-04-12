package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.AssignmentResultResponse;
import ojt.aws.educare.dto.response.AssignmentAttemptResponse;
import ojt.aws.educare.dto.response.SubmissionResponse;
import ojt.aws.educare.entity.Submission;
import ojt.aws.educare.entity.SubmissionAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {

    @Mapping(target = "submissionID", source = "submissionID")
    @Mapping(target = "assignmentId", source = "assignment.assignmentID")
    AssignmentAttemptResponse toAssignmentAttemptResponse(Submission submission);

    @Mapping(target = "assignmentId", source = "submission.assignment.assignmentID")
    @Mapping(target = "assignmentTitle", source = "submission.assignment.title")
    @Mapping(target = "userId", source = "submission.user.userID")
    @Mapping(target = "studentName", source = "submission.user.fullName")
    @Mapping(target = "submitTime", source = "submission.submittedAt")
    @Mapping(target = "answers", source = "answers")
    SubmissionResponse toSubmissionResponse(Submission submission, List<SubmissionResponse.SubmissionAnswerDetail> answers);

    @Mapping(target = "questionId", source = "submissionAnswer.question.id")
    @Mapping(target = "questionText", source = "submissionAnswer.question.questionText")
    @Mapping(target = "answerRefId", source = "submissionAnswer.answerRef.id")
    SubmissionResponse.SubmissionAnswerDetail toSubmissionAnswerDetail(SubmissionAnswer submissionAnswer);

    @Mapping(target = "submissionId", source = "submission.submissionID")
    @Mapping(target = "assignmentId", source = "submission.assignment.assignmentID")
    @Mapping(target = "assignmentTitle", source = "submission.assignment.title")
    @Mapping(target = "userId", source = "submission.user.userID")
    @Mapping(target = "studentName", source = "submission.user.fullName")
    @Mapping(target = "submitTime", source = "submission.submittedAt")
    @Mapping(target = "questions", source = "questions")
    AssignmentResultResponse toAssignmentResultResponse(
            Submission submission,
            Integer totalQuestions,
            Integer correctCount,
            List<AssignmentResultResponse.QuestionResult> questions
    );

    @Mapping(target = "questionId", source = "submissionAnswer.question.id")
    @Mapping(target = "questionText", source = "submissionAnswer.question.questionText")
    @Mapping(target = "selectedAnswerRefId", source = "submissionAnswer.answerRef.id")
    @Mapping(target = "selectedAnswer", source = "submissionAnswer.selectedAnswer")
    AssignmentResultResponse.QuestionResult toQuestionResult(
            SubmissionAnswer submissionAnswer,
            Integer correctAnswerRefId,
            String correctAnswer
    );

    @Mapping(target = "questionId", source = "questionId")
    @Mapping(target = "questionText", source = "questionText")
    @Mapping(target = "selectedAnswerRefId", ignore = true)
    @Mapping(target = "selectedAnswer", ignore = true)
    @Mapping(target = "correctAnswerRefId", source = "correctAnswerRefId")
    @Mapping(target = "correctAnswer", source = "correctAnswer")
    @Mapping(target = "isCorrect", constant = "false")
    AssignmentResultResponse.QuestionResult toMissingQuestionResult(
            Integer questionId,
            String questionText,
            Integer correctAnswerRefId,
            String correctAnswer
    );
}

