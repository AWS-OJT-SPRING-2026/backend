//package ojt.aws.educare.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//import org.hibernate.annotations.CreationTimestamp;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "ai_chat_histories")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class AIChatHistory {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "ChatID")
//    Integer chatID;
//
//    @ManyToOne
//    @JoinColumn(name = "StudentID", nullable = false)
//    Student student;
//
//    @Column(name = "ContextChapter")
//    String contextChapter; // Học sinh đang hỏi trong bối cảnh chương nào
//
//    @Column(name = "QuestionContent", columnDefinition = "TEXT")
//    String questionContent;
//
//    @Column(name = "AiResponse", columnDefinition = "TEXT")
//    String aiResponse;
//
//    @CreationTimestamp
//    @Column(name = "Timestamp", updatable = false)
//    LocalDateTime timestamp;
//}