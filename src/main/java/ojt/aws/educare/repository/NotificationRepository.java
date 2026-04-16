package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Notification;
import ojt.aws.educare.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    @Query("SELECT n FROM Notification n LEFT JOIN n.targetClass tc LEFT JOIN tc.teacher t LEFT JOIN t.user tu " +
           "WHERE n.user.userID = :userId OR " +
           "(tc IS NOT NULL AND (tc.classID IN (SELECT cm.classroom.classID FROM ClassMember cm WHERE cm.student.user.userID = :userId AND cm.status = 'ACTIVE') OR tu.userID = :userId)) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findByUser_UserIDOrderByCreatedAtDesc(@Param("userId") Integer userId);

    @Query("SELECT n FROM Notification n LEFT JOIN n.targetClass tc LEFT JOIN tc.teacher t LEFT JOIN t.user tu " +
           "WHERE (n.user.userID = :userId OR " +
           "(tc IS NOT NULL AND (tc.classID IN (SELECT cm.classroom.classID FROM ClassMember cm WHERE cm.student.user.userID = :userId AND cm.status = 'ACTIVE') OR tu.userID = :userId))) " +
           "AND n.type IN :types ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndTypeIn(
            @Param("userId") Integer userId,
            @Param("types") List<NotificationType> types);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userID = :userId AND n.isRead = false")
    void markAllReadByUserId(@Param("userId") Integer userId);

    long countByUser_UserIDAndIsReadFalse(Integer userId);

    boolean existsByUser_UserIDAndTypeAndActionUrl(Integer userId, NotificationType type, String actionUrl);

    boolean existsByUser_UserIDAndTypeAndActionUrlAndContentAndCreatedAtAfter(
            Integer userId,
            NotificationType type,
            String actionUrl,
            String content,
            LocalDateTime createdAt
    );

    void deleteByActionUrl(String actionUrl);

    long deleteByNotificationIdAndUser_UserID(Integer notificationId, Integer userId);

    long deleteByCreatedAtBefore(LocalDateTime cutoff);
}
