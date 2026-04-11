package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // Weekly progress: assignments (by deadline) and tests (by startTime) within a date range for given classrooms
    @Query("SELECT a FROM Assignment a WHERE a.classroom.classID IN :classroomIds " +
           "AND a.status = 'ACTIVE' " +
           "AND ((a.assignmentType = 'ASSIGNMENT' AND a.deadline BETWEEN :start AND :end) " +
           "OR (a.assignmentType = 'TEST' AND a.startTime BETWEEN :start AND :end))")
    List<Assignment> findWeeklyTasksByClassroomIds(
            @Param("classroomIds") List<Integer> classroomIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Scheduler: assignments with deadline within a time window (for DUE_SOON / OVERDUE)
    @Query("SELECT a FROM Assignment a WHERE a.status = 'ACTIVE' AND a.deadline BETWEEN :start AND :end")
    List<Assignment> findPublishedByDeadlineBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Scheduler: tests with startTime within a time window (for TEST_UPCOMING / TEST_STARTING)
    @Query("SELECT a FROM Assignment a WHERE a.assignmentType = 'TEST' AND a.status = 'ACTIVE' " +
           "AND a.startTime BETWEEN :start AND :end")
    List<Assignment> findPublishedTestsByStartTimeBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
