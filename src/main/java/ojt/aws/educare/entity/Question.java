package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    String questionText;

    @Column(name = "image_url", columnDefinition = "TEXT")
    String imageUrl;

    @Column(name = "explanation", columnDefinition = "TEXT")
    String explanation;

    @Column(name = "difficulty_level", nullable = false)
    Integer difficultyLevel;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "embedding", columnDefinition = "vector")
    float[] embedding;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    QuestionBank bank;

    @Column(name = "is_ai", nullable = false)
    Boolean isAi = false;

    @Builder.Default
    @ManyToMany(mappedBy = "questions")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Assignment> assignments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<SubmissionAnswer> submissionAnswers = new ArrayList<>();
}