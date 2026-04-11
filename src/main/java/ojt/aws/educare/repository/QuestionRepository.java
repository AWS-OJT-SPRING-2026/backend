package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    interface QuestionRandomProjection {
        Integer getId();
        String getQuestionText();
        String getImageUrl();
        String getExplanation();
        Integer getDifficultyLevel();
        Boolean getIsAi();
        Integer getBankId();
        String getBankName();
    }

    List<Question> findByBank_Id(Integer bankId);
    List<Question> findByBank_IdAndDifficultyLevel(Integer bankId, Integer difficultyLevel);

    @Query("SELECT q FROM Question q WHERE (:bankId IS NULL OR q.bank.id = :bankId) AND (:difficultyLevel IS NULL OR q.difficultyLevel = :difficultyLevel)")
    List<Question> findFilteredQuestions(
            @Param("bankId") Integer bankId,
            @Param("difficultyLevel") Integer difficultyLevel
    );

    @Query("""
            SELECT q.id AS id,
                   q.questionText AS questionText,
                   q.imageUrl AS imageUrl,
                   q.explanation AS explanation,
                   q.difficultyLevel AS difficultyLevel,
                   q.isAi AS isAi,
                   b.id AS bankId,
                   b.bankName AS bankName
            FROM Question q
            LEFT JOIN q.bank b
            WHERE (:userId IS NULL OR b.user.userID = :userId)
              AND (:bankId IS NULL OR b.id = :bankId)
              AND (:difficultyLevel IS NULL OR q.difficultyLevel = :difficultyLevel)
            """)
    List<QuestionRandomProjection> findRandomQuestionPreviewData(
            @Param("bankId") Integer bankId,
            @Param("difficultyLevel") Integer difficultyLevel,
            @Param("userId") Integer userId
    );

    @Query("""
            SELECT q.id AS id,
                   q.questionText AS questionText,
                   q.imageUrl AS imageUrl,
                   q.explanation AS explanation,
                   q.difficultyLevel AS difficultyLevel,
                   q.isAi AS isAi,
                   b.id AS bankId,
                   b.bankName AS bankName
            FROM Question q
            LEFT JOIN q.bank b
            WHERE q.id IN :questionIds
            """)
    List<QuestionRandomProjection> findQuestionPreviewDataByIds(@Param("questionIds") List<Integer> questionIds);
}
