package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeacherClassroomOptionResponse {
    Integer classID;
    String className;
    Integer subjectID;
    String subjectName;
}

