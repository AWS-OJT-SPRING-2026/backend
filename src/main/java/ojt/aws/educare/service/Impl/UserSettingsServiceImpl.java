package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.configuration.CurrentUserProvider;
import ojt.aws.educare.dto.request.UserSettingsUpdateRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.UserSettingsResponse;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.entity.UserSettings;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.UserSettingsMapper;
import ojt.aws.educare.repository.UserSettingsRepository;
import ojt.aws.educare.service.UserSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("null")
public class UserSettingsServiceImpl implements UserSettingsService {

    UserSettingsRepository userSettingsRepository;
    CurrentUserProvider currentUserProvider;
    UserSettingsMapper userSettingsMapper;

    private User getCurrentUser() {
        return currentUserProvider.getCurrentUser();
    }

    @Override
    public ApiResponse<UserSettingsResponse> getMySettings() {
        User user = getCurrentUser();
        UserSettings settings = userSettingsRepository.findByUser_UserID(user.getUserID())
                .orElseGet(() -> {
                    // Create default settings if not found
                    UserSettings defaultSettings = UserSettings.builder()
                            .user(user)
                            .theme("light")
                            .language("vi")
                            .sidebarMode("auto")
                            .build();
                    return userSettingsRepository.save(defaultSettings);
                });

        return ApiResponse.<UserSettingsResponse>builder()
                .result(userSettingsMapper.toResponse(settings))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<UserSettingsResponse> updateMySettings(UserSettingsUpdateRequest request) {
        User user = getCurrentUser();
        UserSettings settings = userSettingsRepository.findByUser_UserID(user.getUserID())
                .orElseGet(() -> {
                    UserSettings defaultSettings = UserSettings.builder()
                            .user(user)
                            .theme("light")
                            .language("vi")
                            .sidebarMode("auto")
                            .build();
                    return userSettingsRepository.save(defaultSettings);
                });

        userSettingsMapper.updateSettings(settings, request);
        UserSettings saved = userSettingsRepository.save(settings);

        return ApiResponse.<UserSettingsResponse>builder()
                .result(userSettingsMapper.toResponse(saved))
                .build();
    }
}
