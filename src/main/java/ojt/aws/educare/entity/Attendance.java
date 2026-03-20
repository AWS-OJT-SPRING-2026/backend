package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer attendanceID;

    @ManyToOne
    @JoinColumn(name = "TimetableID", nullable = false)
    Timetable timetable;

    @ManyToOne
    @JoinColumn(name = "StudentID", nullable = false)
    Student student;

    @Column(name = "Status", nullable = false, length = 20)
    String status;

    @Column(name = "Note", length = 255)
    String note;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    LocalDateTime updatedAt;
}