package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "classroom_materials")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassroomMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    Classroom classroom; // Nối với lớp học (VD: 10A1)

    @Column(name = "type")
    String type; // Lưu chữ "THEORY" (Lý thuyết) hoặc "QUESTION" (Câu hỏi)

    // NẾU LÀ LÝ THUYẾT THÌ LƯU VÀO ĐÂY (Để null nếu là câu hỏi)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    Book book;

    // NẾU LÀ CÂU HỎI THÌ LƯU VÀO ĐÂY (Để null nếu là lý thuyết)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_id")
    QuestionBank questionBank;

    // Thêm trường này: Giáo viên nào đã phân phối tài liệu này cho lớp?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    User assignedBy;

    @CreationTimestamp
    @Column(name = "assigned_at", updatable = false)
    LocalDateTime assignedAt;
}