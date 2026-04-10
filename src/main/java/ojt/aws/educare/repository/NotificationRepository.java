package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Notification;
import ojt.aws.educare.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUser_UserIDOrderByCreatedAtDesc(Integer userId);

    @Query("SELECT n FROM Notification n WHERE n.user.userID = :userId AND n.type IN :types ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndTypeIn(
            @Param("userId") Integer userId,
            @Param("types") List<NotificationType> types);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userID = :userId AND n.isRead = false")
    void markAllReadByUserId(@Param("userId") Integer userId);

    long countByUser_UserIDAndIsReadFalse(Integer userId);

    boolean existsByUser_UserIDAndTypeAndActionUrl(Integer userId, NotificationType type, String actionUrl);
}
