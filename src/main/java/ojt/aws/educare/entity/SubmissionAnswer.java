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
    @Column(name = "answerid")
    Integer answerID;

    @ManyToOne
    @JoinColumn(name = "submissionid")
    Submission submission;

    @ManyToOne
    @JoinColumn(name = "questionid")
    Question question;

    @ManyToOne
    @JoinColumn(name = "answer_ref_id")
    Answer answerRef;

    @Column(name = "selected_answer")
    String selectedAnswer;

    @Column(name = "is_correct")
    Boolean isCorrect;
}