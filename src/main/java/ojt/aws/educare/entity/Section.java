package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    Lesson lesson;

    @Column(name = "section_number", length = 10)
    String sectionNumber;

    @Column(name = "section_title", columnDefinition = "TEXT")
    String sectionTitle;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("subsectionNumber ASC")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    List<Subsection> subsections = new ArrayList<>();
}
