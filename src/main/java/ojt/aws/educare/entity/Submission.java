package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SubmissionID")
    Integer submissionID;

    @ManyToOne
    @JoinColumn(name = "AssignmentID", nullable = false)
    Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "StudentID", nullable = false)
    Student student;

    @Column(name = "Score", precision = 5, scale = 2)
    BigDecimal score; // Dùng BigDecimal cho số thập phân (decimal) chính xác nhất

    @Column(name = "TimeTaken")
    Integer timeTaken; // Lưu số giây làm bài

    @CreationTimestamp
    @Column(name = "SubmitTime", updatable = false)
    LocalDateTime submitTime;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<SubmissionAnswer> submissionAnswers = new ArrayList<>();
}