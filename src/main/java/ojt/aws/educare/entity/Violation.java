package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "violations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Violation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ViolationID")
    Integer violationID;

    @Column(name = "Username")
    String username;

    @Column(name = "FullName")
    String fullName;

    @Column(name = "RoleName")
    String roleName;

    @Column(name = "ViolationType")
    String violationType;

    @Column(name = "Description")
    String description;

    @Column(name = "Action")
    String action;

    @Column(name = "BannedUntil")
    LocalDateTime bannedUntil;

    @Column(name = "IsResolved")
    boolean isResolved;

    @CreationTimestamp
    @Column(name = "CreatedAt")
    LocalDateTime createdAt;
}
