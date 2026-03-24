package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Violation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViolationRepository extends JpaRepository<Violation, Integer> {
    List<Violation> findAllByOrderByCreatedAtDesc();
}
