package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.UserSettingsUpdateRequest;
import ojt.aws.educare.dto.response.UserSettingsResponse;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.entity.UserSettings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserSettingsMapper {

    UserSettingsResponse toResponse(UserSettings settings);

    @Mapping(target = "settingID", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateSettings(@MappingTarget UserSettings settings, UserSettingsUpdateRequest request);

    default UserSettings toDefaultSettings(User user) {
        return UserSettings.builder()
                .user(user)
                .theme("light")
                .language("vi")
                .sidebarMode("auto")
                .build();
    }
}
