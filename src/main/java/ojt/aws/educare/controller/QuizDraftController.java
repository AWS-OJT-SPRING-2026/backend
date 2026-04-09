package ojt.aws.educare.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ojt.aws.educare.dto.request.SaveDraftRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.QuizDraftResponse;
import ojt.aws.educare.service.QuizDraftService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quiz-drafts")
@RequiredArgsConstructor
public class QuizDraftController {

    private final QuizDraftService quizDraftService;

    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<QuizDraftResponse> saveDraft(
            @PathVariable Integer assignmentId,
            @RequestBody @Valid SaveDraftRequest request) {
        return quizDraftService.saveDraft(assignmentId, request);
    }

    @GetMapping("/{assignmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<QuizDraftResponse> getDraft(@PathVariable Integer assignmentId) {
        return quizDraftService.getDraft(assignmentId);
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<Void> deleteDraft(@PathVariable Integer assignmentId) {
        return quizDraftService.deleteDraft(assignmentId);
    }
}
