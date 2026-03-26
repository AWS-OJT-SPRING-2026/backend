package ojt.aws.educare.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSettingsUpdateRequest {
    String theme;
    String language;
    String sidebarMode;
}
