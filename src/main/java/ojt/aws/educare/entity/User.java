package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    Integer userID;

    @Column(name = "UserName", nullable = false, unique = true)
    String username;

    @Column(name = "Password", nullable = false)
    String password;

    @Column(name = "FullName", nullable = false, length = 100)
    String fullName;

    @Column(name = "Email", nullable = false, unique = true)
    String email;

    @Column(name = "Phone", nullable = false)
    String phone;

    @ManyToOne
    @JoinColumn(name = "RoleID", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Role role;

    @Column(name = "Status", nullable = false)
    String status;

    @Column(name = "AvatarUrl", length = 500)
    String avatarUrl;

    @Column(name = "LastActiveAt")
    LocalDateTime lastActiveAt;

    @Column(name = "LastPasswordChangeAt")
    LocalDateTime lastPasswordChangeAt;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Teacher teacher;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Student student;
}