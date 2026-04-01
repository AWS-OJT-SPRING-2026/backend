package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "content_blocks")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContentBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subsection_id")
    Subsection subsection;

    @Column(name = "content", columnDefinition = "TEXT")
    String content;

    // Keep this field for app-level model compatibility, but skip JPA mapping
    // to avoid pgvector deserialization issues in this Spring Boot service.
    // Previous mapping (kept as note):
    // @JdbcTypeCode(SqlTypes.VECTOR)
    // @Column(name = "embedding", columnDefinition = "vector")
    @Transient
    float[] embedding;
}
