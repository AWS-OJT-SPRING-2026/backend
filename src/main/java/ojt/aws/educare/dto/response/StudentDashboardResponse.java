package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentDashboardResponse {
    int streakCount;
    List<StreakDayDTO> streakDays;
    
    int pomodoroSessions;
    int totalFocusMinutes;

    List<RoadmapStepDTO> roadmapSteps;
    int completedRoadmapSteps;
    int totalRoadmapSteps;

    List<DeadlineItemDTO> deadlines;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StreakDayDTO {
        String label;
        boolean done;
        boolean today;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoadmapStepDTO {
        int week;
        String title;
        boolean done;
        boolean current;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeadlineItemDTO {
        long id;
        String subject;
        String color;
        String title;
        String due;
        boolean urgent;
        String action;
        boolean missing;
    }
}
