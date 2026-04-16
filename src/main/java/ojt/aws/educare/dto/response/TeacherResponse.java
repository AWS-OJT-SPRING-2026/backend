package ojt.aws.educare.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeacherResponse {
    Integer teacherID;
    String username;
    String email;
    String phone;
    String status;
    String avatarUrl;
    LocalDateTime createdAt;
    UserResponse.RoleResponse role;

    // --- Thông tin chuyên môn ---
    String fullName;
    String specialization;
    String gender;
    boolean isHomeroomTeacher;
    LocalDate dateOfBirth;
    String address;

    // --- Lớp giảng dạy ---
    List<String> classes;
}