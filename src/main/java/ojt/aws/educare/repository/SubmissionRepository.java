package ojt.aws.educare.repository;

import ojt.aws.educare.repository.projection.WeeklyGradeAggregationProjection;
import ojt.aws.educare.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    List<Submission> findByAssignment_AssignmentID(Integer assignmentId);
    List<Submission> findByUser_UserID(Integer userId);
    Optional<Submission> findByAssignment_AssignmentIDAndUser_UserID(Integer assignmentId, Integer userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Submission> findWithLockByAssignment_AssignmentIDAndUser_UserID(Integer assignmentId, Integer userId);
    boolean existsByAssignment_AssignmentIDAndUser_UserID(Integer assignmentId, Integer userId);
    long countByAssignment_AssignmentID(Integer assignmentId);

    List<Submission> findByAssignment_AssignmentIDIn(List<Integer> assignmentIds);

    @Query(value = """
            SELECT
                CAST(EXTRACT(ISODOW FROM s.submitted_at) AS INTEGER) AS dayOfWeek,
                SUM(CASE WHEN s.score >= 6.5 THEN 1 ELSE 0 END) AS hocSinhGioiKha,
                SUM(CASE WHEN s.score < 6.5 THEN 1 ELSE 0 END) AS hocSinhYeuKem,
                COUNT(*) AS tongBaiCham
            FROM submissions s
            JOIN assignments a ON a.assignmentid = s.assignmentid
            WHERE a.classid = :classId
              AND s.score IS NOT NULL
              AND s.submitted_at >= :start
              AND s.submitted_at < :end
            GROUP BY CAST(EXTRACT(ISODOW FROM s.submitted_at) AS INTEGER)
            ORDER BY dayOfWeek
            """, nativeQuery = true)
    List<WeeklyGradeAggregationProjection> aggregateWeeklyGrades(
            @Param("classId") Integer classId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
