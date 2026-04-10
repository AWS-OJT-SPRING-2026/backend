package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeeklyProgressResponse {
    int progressPercent;
    int totalTasks;
    int completedTasks;
    Breakdown breakdown;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Breakdown {
        int assignmentDone;
        int assignmentTotal;
        int testDone;
        int testTotal;
        int attendanceDone;
        int attendanceTotal;
    }
}
