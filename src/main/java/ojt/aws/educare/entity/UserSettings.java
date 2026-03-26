package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SettingID")
    Integer settingID;

    @OneToOne
    @JoinColumn(name = "UserID", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User user;

    @Column(name = "Theme", nullable = false, length = 10)
    @Builder.Default
    String theme = "light";

    @Column(name = "Language", nullable = false, length = 5)
    @Builder.Default
    String language = "vi";

    @Column(name = "SidebarMode", nullable = false, length = 10)
    @Builder.Default
    String sidebarMode = "auto";

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    LocalDateTime updatedAt;
}
