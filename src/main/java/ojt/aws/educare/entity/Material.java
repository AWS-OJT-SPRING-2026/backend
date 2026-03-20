package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "materials")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaterialID")
    Integer materialID;

    @ManyToOne
    @JoinColumn(name = "ClassID", nullable = false)
    Classroom classroom;

    @Column(name = "Title", nullable = false, length = 255)
    String title;

    @Column(name = "FileURL")
    String fileURL;

    @ManyToOne
    @JoinColumn(name = "UploadedBy", nullable = false)
    Teacher uploadedBy;

    @CreationTimestamp
    @Column(name = "UploadedAt", updatable = false)
    LocalDateTime uploadedAt;
}
