package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.ChatSessionUpsertRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.ChatSessionResponse;

import java.util.List;

public interface ChatSessionService {
    ApiResponse<List<ChatSessionResponse>> getMySessions();

    ApiResponse<ChatSessionResponse> upsertMySession(ChatSessionUpsertRequest request);
}

