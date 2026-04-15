package ojt.aws.educare.repository;

import ojt.aws.educare.entity.AIChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIChatSessionRepository extends JpaRepository<AIChatSession, Long> {
    List<AIChatSession> findByStudent_StudentIDOrderByUpdatedAtDesc(Integer studentID);

    Optional<AIChatSession> findByStudent_StudentIDAndSessionKey(Integer studentID, String sessionKey);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM AIChatSession a WHERE a.updatedAt < :cutoff")
    void deleteByUpdatedAtBefore(@org.springframework.data.repository.query.Param("cutoff") java.time.LocalDateTime cutoff);
}

