package ojt.aws.educare.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TeacherID")
    Integer teacherID;

    @OneToOne
    @JoinColumn(name = "UserID", nullable = false, unique = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User user;

    @Column(name = "FullName", nullable = false)
    String fullName;

    @Column(name = "Specialization")
    String specialization;

    @Column(name = "Gender", length = 10)
    String gender;

    @Column(name = "IsHomeroomTeacher", nullable = false)
    boolean isHomeroomTeacher; //có phải giao viên chủ nhiệm không

    @Column(name = "DateOfBirth")
    LocalDate dateOfBirth;

    @Column(name = "Address")
    String address;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Classroom> classrooms = new ArrayList<>();

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Timetable> timetables = new ArrayList<>();
}
