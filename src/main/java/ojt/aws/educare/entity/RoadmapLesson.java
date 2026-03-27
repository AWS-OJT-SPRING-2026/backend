package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "roadmap_lessons")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoadmapLesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_chapter_id")
    RoadmapChapter roadmapChapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lessonid")
    Lesson lesson;

    @Column(name = "time")
    Integer time;

    @Column(name = "explanation", columnDefinition = "TEXT")
    String explanation;

    @Column(name = "wrong_question_count")
    Integer wrongQuestionCount;

    @Column(name = "priority_score")
    Float priorityScore;
}
