package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "learning_profiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProfileID")
    Integer profileID;

    @ManyToOne
    @JoinColumn(name = "StudentID", nullable = false)
    Student student;

    @ManyToOne
    @JoinColumn(name = "SubjectID", nullable = false)
    Subject subject;

    // Lưu chuỗi JSON. PostgreSQL sẽ tự động hiểu nếu cấu hình columnDefinition = "json"
    @Column(name = "WeakChapters", columnDefinition = "json")
    String weakChapters;

    @Column(name = "StrongChapters", columnDefinition = "json")
    String strongChapters;

    @UpdateTimestamp
    @Column(name = "LastUpdated")
    LocalDateTime lastUpdated;
}