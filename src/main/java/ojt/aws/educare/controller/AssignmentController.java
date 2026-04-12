package ojt.aws.educare.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.CreateAssignmentRequest;
import ojt.aws.educare.dto.request.SubmitAssignmentRequest;
import ojt.aws.educare.dto.request.UpdateAssignmentRequest;
import ojt.aws.educare.dto.response.*;
import ojt.aws.educare.service.AssignmentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assignments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AssignmentController {

    AssignmentService assignmentService;

    // ===== TEACHER ENDPOINTS =====

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<AssignmentDetailResponse> createAssignment(
            @RequestBody @Valid CreateAssignmentRequest request) {
        return assignmentService.createAssignment(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<AssignmentDetailResponse> updateAssignment(
            @PathVariable Integer id,
            @RequestBody @Valid UpdateAssignmentRequest request) {
        return assignmentService.updateAssignment(id, request);
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<AssignmentResponse> publishAssignment(@PathVariable Integer id) {
        return assignmentService.publishAssignment(id);
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<AssignmentResponse> closeAssignment(@PathVariable Integer id) {
        return assignmentService.closeAssignment(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> deleteAssignment(@PathVariable Integer id) {
        return assignmentService.deleteAssignment(id);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<AssignmentResponse>> getMyAssignments() {
        return assignmentService.getMyAssignments();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ApiResponse<AssignmentDetailResponse> getAssignmentDetail(@PathVariable Integer id) {
        return assignmentService.getAssignmentDetail(id);
    }

    @GetMapping("/{id}/report")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<AssignmentReportResponse> getAssignmentReport(@PathVariable Integer id) {
        return assignmentService.getAssignmentReport(id);
    }

    @GetMapping("/submissions/{submissionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<SubmissionResponse> getSubmissionDetailForTeacher(@PathVariable Integer submissionId) {
        return assignmentService.getSubmissionDetailForTeacher(submissionId);
    }

    @GetMapping("/questions/random")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<QuestionPreviewResponse>> getRandomQuestions(
            @RequestParam(required = false) Integer bankId,
            @RequestParam(required = false) Integer difficultyLevel,
            @RequestParam(defaultValue = "10") Integer limit) {
        return assignmentService.getRandomQuestions(bankId, difficultyLevel, limit);
    }

    @GetMapping("/banks/my")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<QuestionBankResponse>> getMyQuestionBanks() {
        return assignmentService.getMyQuestionBanks();
    }

    // ===== STUDENT ENDPOINTS =====

    @GetMapping("/classroom/{classroomId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<AssignmentResponse>> getAssignmentsForStudent(
            @PathVariable Integer classroomId) {
        return assignmentService.getAssignmentsForStudent(classroomId);
    }

    @GetMapping("/student/active")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<AssignmentResponse>> getStudentActiveAssignments() {
        return assignmentService.getStudentActiveAssignments();
    }

    @GetMapping("/student/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<SubmissionResponse>> getStudentSubmissions() {
        return assignmentService.getStudentSubmissions();
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<AssignmentAttemptResponse> startAssignment(@PathVariable Integer id) {
        return assignmentService.startAssignment(id);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<SubmissionResponse> submitAssignment(
            @PathVariable Integer id,
            @RequestBody @Valid SubmitAssignmentRequest request) {
        return assignmentService.submitAssignment(id, request);
    }

    @GetMapping("/{id}/my-submission")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<SubmissionResponse> getMySubmission(@PathVariable Integer id) {
        return assignmentService.getMySubmission(id);
    }

    @GetMapping("/{id}/results")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<AssignmentResultResponse> getMyResult(@PathVariable Integer id) {
        return assignmentService.getMyResult(id);
    }

    @GetMapping("/student/upcoming-tasks")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<UpcomingTaskResponse>> getUpcomingTasks() {
        return assignmentService.getUpcomingTasks();
    }
}
