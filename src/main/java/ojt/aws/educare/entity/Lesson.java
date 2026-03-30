package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    Chapter chapter;

    @Column(name = "lesson_number", length = 10)
    String lessonNumber;

    @Column(name = "title", columnDefinition = "TEXT")
    String title;

    @Column(name = "estimated_time")
    Integer estimatedTime;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sectionNumber ASC")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    List<Section> sections = new ArrayList<>();
}
