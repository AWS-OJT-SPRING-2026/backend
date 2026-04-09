package ojt.aws.educare.repository;

import ojt.aws.educare.entity.QuizDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizDraftRepository extends JpaRepository<QuizDraft, Integer> {
    Optional<QuizDraft> findBySubmission_SubmissionID(Integer submissionId);
    void deleteBySubmission_SubmissionID(Integer submissionId);
}
