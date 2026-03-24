package ojt.aws.educare.repository;

import ojt.aws.educare.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByUserIdOrderByTimestampDesc(String userId);

    @Query("SELECT COUNT(DISTINCT u.userId) FROM UserActivity u WHERE u.timestamp > :since")
    long countDistinctUserIdsSince(LocalDateTime since);

    @Query("SELECT u FROM UserActivity u WHERE u.action = 'EDIT_SCORE' AND u.timestamp > :since ORDER BY u.timestamp DESC")
    List<UserActivity> findScoreEditsSince(LocalDateTime since);

    long countByUserIdAndActionAndTimestampAfter(String userId, String action, LocalDateTime since);
}
