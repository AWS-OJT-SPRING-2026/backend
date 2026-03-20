package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimetableResponse {
    Integer timetableID;
    Integer classID;
    String className;
    String subjectName;
    Integer teacherID;
    String teacherName;
    String status;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String topic;
    String googleMeetLink;
}