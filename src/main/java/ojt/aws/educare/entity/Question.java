package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    @Column(name = "QuestionID")
    Integer questionID;

    @ManyToOne
    @JoinColumn(name = "SubjectID", nullable = false)
    Subject subject;

    @Column(name = "Chapter", nullable = false)
    String chapter;

    @Column(name = "Level")
    String level; // EASY, MEDIUM, HARD

    @Column(name = "Tags")
    String tags;

    @Column(name = "Content", nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(name = "Options", columnDefinition = "json")
    String options;

    @Column(name = "CorrectAnswer", nullable = false)
    String correctAnswer;

    @ManyToMany(mappedBy = "questions")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Assignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<SubmissionAnswer> submissionAnswers = new ArrayList<>();
}