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
    @Column(name = "submissionid")
    Integer submissionID;

    @ManyToOne
    @JoinColumn(name = "assignmentid")
    Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "userid")
    User user;

    @Column(name = "score", precision = 5, scale = 2)
    BigDecimal score;

    @Column(name = "time_taken")
    Integer timeTaken;

    @CreationTimestamp
    @Column(name = "submit_time", updatable = false)
    LocalDateTime submitTime;

    @Builder.Default
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<SubmissionAnswer> submissionAnswers = new ArrayList<>();
}