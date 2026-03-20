package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    Optional<Subject> findBySubjectName(String subjectName);
    boolean existsBySubjectName(String subjectName);
}