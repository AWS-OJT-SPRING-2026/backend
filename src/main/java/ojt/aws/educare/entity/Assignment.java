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
@Table(name = "assignments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AssignmentID")
    Integer assignmentID;

    @ManyToOne
    @JoinColumn(name = "ClassID", nullable = false)
    Classroom classroom;

    @ManyToOne
    @JoinColumn(name = "TeacherID", nullable = false)
    Teacher teacher;

    @Column(name = "Title", nullable = false, length = 255)
    String title;

    @Column(name = "Deadline", nullable = false)
    LocalDateTime deadline;

    @Column(name = "Type", length = 100)
    String type;

    @Column(name = "Status", length = 50)
    String status;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "assignment_questions",
            joinColumns = @JoinColumn(name = "AssignmentID"),
            inverseJoinColumns = @JoinColumn(name = "QuestionID")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Submission> submissions = new ArrayList<>();
}
