package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassroomStatsResponse {
    long totalClasses;        // Tổng số lớp
    long activeClasses;       // Đang hoạt động
    long unassignedClasses;   // Chưa có giáo viên
    int averageClassSize;     // Sĩ số trung bình
}