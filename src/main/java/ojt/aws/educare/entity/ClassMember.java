package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "classMember")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClassMember {
    @EmbeddedId
    ClassMemberID classMemberID;

    @ManyToOne
    @MapsId("classID")
    @JoinColumn(name = "ClassID", nullable = false)
    Classroom classroom;

    @ManyToOne
    @MapsId("studentID")
    @JoinColumn(name = "StudentID", nullable = false)
    Student student;

    @CreationTimestamp
    @Column(name = "JoinedAt", updatable = false)
    LocalDateTime joinedAt;

    @Column(name = "Status", nullable = false, length = 100)
    String status;
}
