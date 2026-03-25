package ojt.aws.educare.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatSessionResponse {
    String sessionId;
    String title;
    List<ChatMessageResponse> messages;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

