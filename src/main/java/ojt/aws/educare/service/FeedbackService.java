package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.FeedbackRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.FeedbackResponse;

import java.util.List;

public interface FeedbackService {
    ApiResponse<FeedbackResponse> createFeedback(FeedbackRequest request);
    ApiResponse<List<FeedbackResponse>> getFeedbackByStudent(Integer studentId);
    ApiResponse<List<FeedbackResponse>> getMyFeedbackForAssignment(Integer assignmentId);
}
