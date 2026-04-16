package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentScheduleResponse {
    Integer timetableID;
    Integer classID;
    String classStatus;
    String subjectName;
    String className;
    String teacherName;

    LocalDateTime startTime;
    LocalDateTime endTime;

    String room;
    String topic;
    String meetUrl;

    String attendanceStatus;

    Integer studentCount;
    List<ClassmateResponse> classmates;

    @Data
    @Builder
    public static class ClassmateResponse {
        Integer studentID;
        String fullName;
        String avatarUrl;
    }
}