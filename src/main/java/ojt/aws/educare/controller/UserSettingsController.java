package ojt.aws.educare.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.UserSettingsUpdateRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.UserSettingsResponse;
import ojt.aws.educare.service.UserSettingsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/settings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserSettingsController {

    UserSettingsService userSettingsService;

    @GetMapping
    public ApiResponse<UserSettingsResponse> getMySettings() {
        return userSettingsService.getMySettings();
    }

    @PutMapping
    public ApiResponse<UserSettingsResponse> updateMySettings(@RequestBody UserSettingsUpdateRequest request) {
        return userSettingsService.updateMySettings(request);
    }
}
