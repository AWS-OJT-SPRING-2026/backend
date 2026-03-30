package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.DocumentDistributionUpdateRequest;
import ojt.aws.educare.dto.response.ApiResponse;

public interface DocumentService {
    ApiResponse<Void> updateDistributions(Integer documentId, DocumentDistributionUpdateRequest request);
}

