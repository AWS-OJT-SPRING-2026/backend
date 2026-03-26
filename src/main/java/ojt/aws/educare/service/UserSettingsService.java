package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.UserSettingsUpdateRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.UserSettingsResponse;

public interface UserSettingsService {
    ApiResponse<UserSettingsResponse> getMySettings();
    ApiResponse<UserSettingsResponse> updateMySettings(UserSettingsUpdateRequest request);
}
