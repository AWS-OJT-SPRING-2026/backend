package ojt.aws.educare.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentResponse {
    // --- Account Info ---
    Integer studentID;
    String username;
    String email;
    String phone;
    String status;
    String avatarUrl;
    LocalDateTime createdAt;
    UserResponse.RoleResponse role;

    // --- Personal Info ---
    String fullName;
    LocalDate dateOfBirth;
    String gender;
    String address;

    // --- Parent Info ---
    String parentName;
    String parentPhone;
    String parentEmail;
    String parentRelationship;
}