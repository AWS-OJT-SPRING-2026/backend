package ojt.aws.educare.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatSessionUpsertRequest {
    String sessionId;
    String title;
    List<ChatMessageRequest> messages;
}

