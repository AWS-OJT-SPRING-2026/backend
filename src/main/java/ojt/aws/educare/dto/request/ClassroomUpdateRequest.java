package ojt.aws.educare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassroomUpdateRequest {
    @NotBlank(message = "CLASSNAME_REQUIRED")
    String className;

    @NotNull(message = "SUBJECT_ID_REQUIRED")
    Integer subjectID;

    @NotBlank(message = "SEMESTER_REQUIRED")
    String semester;

    @NotBlank(message = "ACADEMIC_YEAR_REQUIRED")
    String academicYear;

    LocalDate startDate;
    LocalDate endDate;

    @NotNull(message = "MAX_STUDENTS_REQUIRED")
    Integer maxStudents;

    Integer teacherID;

    String status; // ACTIVE, INACTIVE, COMPLETED...
}