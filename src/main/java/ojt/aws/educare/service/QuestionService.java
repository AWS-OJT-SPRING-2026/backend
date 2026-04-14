package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.QuestionCreateRequest;
import ojt.aws.educare.dto.request.QuestionUpdateRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.QuestionPreviewResponse;

import java.util.List;

public interface QuestionService {
    ApiResponse<List<QuestionPreviewResponse>> getQuestionsByBankId(Integer bankId);
    ApiResponse<QuestionPreviewResponse> createQuestion(Integer bankId, QuestionCreateRequest request);
    ApiResponse<QuestionPreviewResponse> updateQuestion(Integer questionId, QuestionUpdateRequest request);
    ApiResponse<Void> deleteQuestion(Integer questionId);
}
