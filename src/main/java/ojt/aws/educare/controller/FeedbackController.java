package ojt.aws.educare.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.FeedbackRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.FeedbackResponse;
import ojt.aws.educare.service.FeedbackService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher/feedback")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackController {

    FeedbackService feedbackService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<FeedbackResponse> createFeedback(@RequestBody @Valid FeedbackRequest request) {
        return feedbackService.createFeedback(request);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ApiResponse<List<FeedbackResponse>> getFeedbackByStudent(@PathVariable Integer studentId) {
        return feedbackService.getFeedbackByStudent(studentId);
    }

    @GetMapping("/my/assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    public ApiResponse<List<FeedbackResponse>> getMyFeedbackForAssignment(@PathVariable Integer assignmentId) {
        return feedbackService.getMyFeedbackForAssignment(assignmentId);
    }
}
