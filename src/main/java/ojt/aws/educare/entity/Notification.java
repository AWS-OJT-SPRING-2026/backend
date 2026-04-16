package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    Integer notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    NotificationType type;

    @Column(name = "title", nullable = false, length = 255)
    String title;

    @Column(name = "content", length = 1000)
    String content;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    Boolean isRead = Boolean.FALSE;

    @Column(name = "action_url", length = 500)
    String actionUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_class_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Classroom targetClass;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "notification_reads", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "user_id")
    @Builder.Default
    Set<Integer> readByUsers = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
}
