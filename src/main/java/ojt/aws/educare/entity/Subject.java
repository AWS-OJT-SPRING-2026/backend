package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SubjectID")
    Integer subjectID;

    @Column(name = "SubjectName", nullable = false, unique = true, length = 100)
    String subjectName;

    @Column(name = "Description", length = 500)
    String description;

    @Column(name = "GradeLevel")
    Integer gradeLevel;

    @Column(name = "Credits")
    Integer credits;

    @Column(name = "IsActive")
    @Builder.Default
    Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Classroom> classrooms = new ArrayList<>();

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<LearningProfile> learningProfiles = new ArrayList<>();
}
