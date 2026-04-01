package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

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
}
