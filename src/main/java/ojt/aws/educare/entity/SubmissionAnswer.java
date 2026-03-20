package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "submission_answers")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmissionAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AnswerID")
    Integer answerID;

    @ManyToOne
    @JoinColumn(name = "SubmissionID", nullable = false)
    Submission submission;

    @ManyToOne
    @JoinColumn(name = "QuestionID", nullable = false)
    Question question;

    @Column(name = "SelectedAnswer")
    String selectedAnswer;

    @Column(name = "IsCorrect")
    Boolean isCorrect;
}