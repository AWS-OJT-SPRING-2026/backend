package ojt.aws.educare.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.SubmissionScoreRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.entity.Submission;
import ojt.aws.educare.repository.SubmissionRepository;
import ojt.aws.educare.service.MonitoringService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubmissionController {
    SubmissionRepository submissionRepository;
    MonitoringService monitoringService;

    @PutMapping("/{submissionId}/score")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Submission> updateScore(
            @PathVariable Integer submissionId,
            @RequestBody SubmissionScoreRequest request) {

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        String oldScore = submission.getScore() != null ? submission.getScore().toString() : "0";
        submission.setScore(request.getScore());
        submissionRepository.save(submission);

        // Explicitly track score edit for monitoring
        String teacherId = SecurityContextHolder.getContext().getAuthentication().getName();
        monitoringService.trackActivity(teacherId, "EDIT_SCORE",
                "Updated submission " + submissionId + " score from " + oldScore + " to " + request.getScore(),
                "System-Teacher", "INTERNAL");

        return ApiResponse.<Submission>builder()
                .result(submission)
                .message("Cập nhật điểm thành công")
                .build();
    }
}
