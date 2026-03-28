package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentWeeklyStatsResponse {
    int totalClassesThisWeek; // Tiết học tuần này
    double totalHoursStudied; // Giờ học (chỉ tính khi đã điểm danh CÓ MẶT)
    int totalSubjects;        // Môn học
    int totalExams;           // Bài kiểm tra (Tạm để 0 nếu bạn chưa làm bảng Exam)
}