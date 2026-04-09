package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.SaveDraftRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.QuizDraftResponse;

public interface QuizDraftService {

    ApiResponse<QuizDraftResponse> saveDraft(Integer assignmentId, SaveDraftRequest request);
    ApiResponse<QuizDraftResponse> getDraft(Integer assignmentId);
    ApiResponse<Void> deleteDraft(Integer assignmentId);
}
