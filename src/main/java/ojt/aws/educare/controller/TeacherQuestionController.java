package ojt.aws.educare.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.QuestionCreateRequest;
import ojt.aws.educare.dto.request.QuestionUpdateRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.QuestionPreviewResponse;
import ojt.aws.educare.service.QuestionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherQuestionController {

    QuestionService questionService;

    @GetMapping("/question-banks/{bankId}/questions")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<QuestionPreviewResponse>> getQuestionsByBankId(@PathVariable Integer bankId) {
        return questionService.getQuestionsByBankId(bankId);
    }

    @PostMapping("/question-banks/{bankId}/questions")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<QuestionPreviewResponse> createQuestion(
            @PathVariable Integer bankId,
            @RequestBody @Valid QuestionCreateRequest request) {
        return questionService.createQuestion(bankId, request);
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<QuestionPreviewResponse> updateQuestion(
            @PathVariable Integer questionId,
            @RequestBody @Valid QuestionUpdateRequest request) {
        return questionService.updateQuestion(questionId, request);
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> deleteQuestion(@PathVariable Integer questionId) {
        return questionService.deleteQuestion(questionId);
    }
}
