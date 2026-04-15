package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.CreateAssignmentRequest;
import ojt.aws.educare.dto.request.SubmitAssignmentRequest;
import ojt.aws.educare.dto.request.UpdateAssignmentRequest;
import ojt.aws.educare.dto.response.*;

import java.util.List;

public interface AssignmentService {
    ApiResponse<AssignmentDetailResponse> createAssignment(CreateAssignmentRequest request);
    ApiResponse<AssignmentDetailResponse> updateAssignment(Integer assignmentId, UpdateAssignmentRequest request);
    ApiResponse<AssignmentResponse> publishAssignment(Integer assignmentId);
    ApiResponse<AssignmentResponse> closeAssignment(Integer assignmentId);
    ApiResponse<Void> deleteAssignment(Integer assignmentId);
    ApiResponse<List<AssignmentResponse>> getMyAssignments();
    ApiResponse<AssignmentDetailResponse> getAssignmentDetail(Integer assignmentId);
    ApiResponse<List<QuestionPreviewResponse>> getRandomQuestions(Integer bankId, Integer difficultyLevel, Integer limit, Integer classroomId);
    ApiResponse<AssignmentReportResponse> getAssignmentReport(Integer assignmentId);
    ApiResponse<SubmissionResponse> getSubmissionDetailForTeacher(Integer submissionId);
    ApiResponse<List<AssignmentResponse>> getAssignmentsForStudent(Integer classroomId);
    ApiResponse<List<AssignmentResponse>> getStudentActiveAssignments();
    ApiResponse<List<SubmissionResponse>> getStudentSubmissions();
    ApiResponse<AssignmentAttemptResponse> startAssignment(Integer assignmentId);
    ApiResponse<SubmissionResponse> submitAssignment(Integer assignmentId, SubmitAssignmentRequest request);
    ApiResponse<SubmissionResponse> getMySubmission(Integer assignmentId);
    ApiResponse<AssignmentResultResponse> getMyResult(Integer assignmentId);
    ApiResponse<List<QuestionBankResponse>> getMyQuestionBanks();
    ApiResponse<List<UpcomingTaskResponse>> getUpcomingTasks();
}
