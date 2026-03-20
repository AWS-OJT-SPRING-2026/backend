package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassroomResponse {
    Integer classID;
    String className;
    String subjectName;
    String semester;
    String academicYear;
    LocalDate startDate;
    LocalDate endDate;
    String status;
    Integer maxStudents;
    Integer currentStudents;
    String teacherName;
}