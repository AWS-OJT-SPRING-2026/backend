package ojt.aws.educare.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_content_blocks")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(QuestionContentBlockId.class)
public class QuestionContentBlock {

    @Id
    @Column(name = "questionid")
    Integer questionId;

    @Id
    @Column(name = "content_block_id")
    Integer contentBlockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "questionid", insertable = false, updatable = false)
    Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_block_id", insertable = false, updatable = false)
    ContentBlock contentBlock;

    @Column(name = "similarity_score", nullable = false)
    Float similarityScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
}
