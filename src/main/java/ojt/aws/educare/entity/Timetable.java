package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timetables")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TimetableID")
    Integer timetableID;

    @ManyToOne
    @JoinColumn(name = "ClassID", nullable = false)
    Classroom classroom;

    @ManyToOne
    @JoinColumn(name = "TeacherID")
    Teacher teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    TimetableStatus status;

    @Column(name = "StartTime", nullable = false)
    LocalDateTime startTime;

    @Column(name = "EndTime", nullable = false)
    LocalDateTime endTime;

    @Column(name = "Topic", length=200)
    String topic;

    @Column(name = "GoogleMeetLink")
    String googleMeetLink;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    LocalDateTime createdAt;

    @OneToMany(mappedBy = "timetable", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<Attendance> attendances = new ArrayList<>();
}