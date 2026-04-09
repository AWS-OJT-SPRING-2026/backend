package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "quiz_drafts",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_quiz_draft_submission",
        columnNames = {"submission_id"}
    )
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuizDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Submission submission;

    @Column(name = "answers_json", columnDefinition = "TEXT")
    String answersJson;

    @Column(name = "current_question")
    Integer currentQuestion;

    @Column(name = "last_saved_at")
    LocalDateTime lastSavedAt;
}
