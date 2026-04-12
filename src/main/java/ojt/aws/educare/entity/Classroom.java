package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classrooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClassID")
    Integer classID;

    @Column(name = "ClassName", nullable = false, length = 50)
    String className;

    @Column(name = "Status", nullable = false, length = 100)
    String status;

    @Column(name = "StartDate")
    LocalDate startDate;

    @Column(name = "EndDate")
    LocalDate endDate;

    @Column(name = "Semester", nullable = false, length = 20)
    String semester;

    @Column(name = "AcademicYear", nullable = false, length = 20)
    String academicYear;

    @Column(name = "MaxStudents")
    Integer maxStudents;

    @ManyToOne
    @JoinColumn(name = "SubjectID")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Subject subject;

    @ManyToOne
    @JoinColumn(name = "TeacherID")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Teacher teacher;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<ClassMember> classMembers = new ArrayList<>();

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Assignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Timetable> timetables = new ArrayList<>();
}
