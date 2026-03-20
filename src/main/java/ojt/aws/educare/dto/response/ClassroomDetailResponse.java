package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassroomDetailResponse {
    Integer classID;
    String className;
    Integer subjectID;
    String subjectName;
    Integer teacherID;
    String teacherName;
    String semester;
    String academicYear;
    LocalDate startDate;
    LocalDate endDate;
    String status;
    Integer maxStudents;
    Integer currentStudents;

    List<StudentInClassResponse> students;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentInClassResponse {
        Integer studentID;
        String fullName;
        String gender;
        String email;
        String phone;
        String memberStatus;
    }
}