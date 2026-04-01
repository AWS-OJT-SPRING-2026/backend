package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
    List<Assignment> findByUser_UserIDOrderByCreatedAtDesc(Integer userId);
    List<Assignment> findByClassroom_ClassIDOrderByCreatedAtDesc(Integer classId);
    List<Assignment> findByClassroom_ClassIDAndStatus(Integer classId, String status);

    @Query(value = "SELECT questionid FROM assignment_questions WHERE assignmentid = :assignmentId", nativeQuery = true)
    List<Integer> findQuestionIdsByAssignmentId(@Param("assignmentId") Integer assignmentId);

    @Query(value = "SELECT COUNT(*) FROM assignment_questions WHERE assignmentid = :assignmentId", nativeQuery = true)
    long countQuestionsByAssignmentId(@Param("assignmentId") Integer assignmentId);

    @Modifying
    @Query(value = "INSERT INTO assignment_questions (assignmentid, questionid) VALUES (:assignmentId, :questionId)", nativeQuery = true)
    void insertAssignmentQuestion(
            @Param("assignmentId") Integer assignmentId,
            @Param("questionId") Integer questionId
    );

    @Modifying
    @Query(value = "DELETE FROM assignment_questions WHERE assignmentid = :assignmentId", nativeQuery = true)
    void deleteAssignmentQuestionsByAssignmentId(@Param("assignmentId") Integer assignmentId);
}
