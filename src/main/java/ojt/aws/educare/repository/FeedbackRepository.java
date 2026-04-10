package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {

    List<Feedback> findByStudent_StudentIDOrderByCreatedAtDesc(Integer studentId);

    List<Feedback> findByAssignment_AssignmentIDAndStudent_StudentID(Integer assignmentId, Integer studentId);
}
