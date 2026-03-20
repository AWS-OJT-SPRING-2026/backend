package ojt.aws.educare.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Integer userID;
    String username;
    String fullName;
    String email;
    String phone;
    String password;
    String status;
    LocalDateTime createdAt;
    List<String> classes;
    RoleResponse role;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleResponse {
        Integer roleID;
        String roleName;
        String description;
    }
}
