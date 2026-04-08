package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignmentid")
    Integer assignmentID;

    @ManyToOne
    @JoinColumn(name = "classid")
    Classroom classroom;

    @ManyToOne
    @JoinColumn(name = "userid", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "teacherid", nullable = false)
    Teacher teacher;

    @Column(name = "title", length = 255)
    String title;

    @Column(name = "assignment_type", length = 50)
    String assignmentType;

    @Column(name = "format", length = 50)
    String format;

    @Column(name = "start_time")
    LocalDateTime startTime;

    @Column(name = "end_time")
    LocalDateTime endTime;

    @Column(name = "deadline")
    LocalDateTime deadline;

    @Column(name = "duration_minutes")
    Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_answer_mode", length = 40, nullable = false)
    @Builder.Default
    DisplayAnswerMode displayAnswerMode = DisplayAnswerMode.IMMEDIATE;

    @Column(name = "status", length = 50)
    String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "assignment_questions",
            joinColumns = @JoinColumn(name = "assignmentid"),
            inverseJoinColumns = @JoinColumn(name = "questionid")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Question> questions = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Submission> submissions = new ArrayList<>();
}
