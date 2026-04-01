package ojt.aws.educare.repository;

import ojt.aws.educare.entity.SubmissionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionAnswerRepository extends JpaRepository<SubmissionAnswer, Integer> {
    List<SubmissionAnswer> findBySubmission_SubmissionID(Integer submissionId);

    @Query("""
            SELECT sa.question.id, COUNT(sa), SUM(CASE WHEN sa.isCorrect = true THEN 1 ELSE 0 END)
            FROM SubmissionAnswer sa
            WHERE sa.submission.assignment.assignmentID = :assignmentId
            GROUP BY sa.question.id
            """)
    List<Object[]> countCorrectByQuestionForAssignment(@Param("assignmentId") Integer assignmentId);
}
