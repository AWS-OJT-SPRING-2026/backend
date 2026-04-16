package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeacherUpdateProfileRequest {
    @NotBlank(message = "FULLNAME_REQUIRED")
    String fullName;

    @Email(message = "INVALID_EMAIL_FORMAT")
    @NotBlank(message = "EMAIL_REQUIRED")
    String email;

    String phone;
    
    String gender;
    String specialization;
    LocalDate dateOfBirth;
    String address;
}
