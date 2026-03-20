package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "roadmaps")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Roadmap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoadmapID")
    Integer roadmapID;

    @ManyToOne
    @JoinColumn(name = "StudentID", nullable = false)
    Student student;

    @Column(name = "TargetScore", precision = 5, scale = 2)
    BigDecimal targetScore;

    @Column(name = "StudyTimeframe")
    String studyTimeframe;

    @Column(name = "GeneratedPlan", columnDefinition = "json")
    String generatedPlan;

    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    LocalDateTime createdAt;
}