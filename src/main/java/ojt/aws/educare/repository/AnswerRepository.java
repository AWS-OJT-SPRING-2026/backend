package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    List<Answer> findByQuestion_Id(Integer questionId);
    List<Answer> findByQuestion_IdIn(List<Integer> questionIds);
}
