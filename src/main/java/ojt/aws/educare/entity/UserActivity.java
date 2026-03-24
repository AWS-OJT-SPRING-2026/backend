package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "UserId")
    String userId;

    @Column(name = "Action")
    String action;

    @Column(name = "Details", columnDefinition = "TEXT")
    String details;

    @Column(name = "IpAddress")
    String ipAddress;

    @CreationTimestamp
    @Column(name = "Timestamp")
    LocalDateTime timestamp;
}
