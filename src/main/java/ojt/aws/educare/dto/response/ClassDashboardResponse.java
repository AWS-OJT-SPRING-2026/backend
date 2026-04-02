package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassDashboardResponse {
    Integer classID;
    String className;
    Integer totalStudents;
    Integer onlineStudents;
    Integer offlineStudents;
    Integer attentionStudents;
    Double averageGpa;
}


