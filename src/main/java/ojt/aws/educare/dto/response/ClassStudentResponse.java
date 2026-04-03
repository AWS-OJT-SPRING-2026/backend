package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassStudentResponse {
    Integer studentId;
    String fullName;
    String mssv;
    String avatarUrl;
    Double completionRate;
    Double gpa;
    Integer missingCount;
    LocalDateTime lastActiveTime;
    String status;
}


