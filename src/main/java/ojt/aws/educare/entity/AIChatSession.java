package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_sessions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"StudentID", "SessionKey"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AIChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ChatSessionID")
    Long chatSessionID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Student student;

    @Column(name = "SessionKey", nullable = false, length = 120)
    String sessionKey;

    @Column(name = "Title", nullable = false, length = 255)
    String title;

    @Column(name = "MessagesJson", columnDefinition = "TEXT")
    String messagesJson;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    LocalDateTime updatedAt;
}

