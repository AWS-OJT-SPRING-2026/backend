package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StudentID")
    Integer studentID;

    @OneToOne
    @JoinColumn(name = "UserID", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    User user;

    @Column(name = "FullName", nullable = false, length = 255)
    String fullName;

    @Column(name = "DateOfBirth")
    LocalDate dateOfBirth;

    @Column(name = "Gender", length = 10)
    String gender;

    @Column(name = "Address", columnDefinition = "TEXT")
    String address;

    @Column(name = "ParentName", length = 255)
    String parentName;

    @Column(name = "ParentPhone", length = 15)
    String parentPhone;

    @Column(name = "ParentEmail", length = 255)
    String parentEmail;

    @Column(name = "ParentRelationship", length = 50)
    String parentRelationship;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<ClassMember> classMembers = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Submission> submissions = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<AIChatHistory> aiChatHistories = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Roadmap> roadmaps = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<LearningProfile> learningProfiles = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Attendance> attendances = new ArrayList<>();

}
