package ojt.aws.educare.repository;

import ojt.aws.educare.entity.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Integer> {
}

