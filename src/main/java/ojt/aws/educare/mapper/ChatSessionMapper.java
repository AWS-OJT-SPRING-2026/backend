package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.ChatMessageRequest;
import ojt.aws.educare.dto.response.ChatMessageResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatSessionMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessageRequest message);

    List<ChatMessageResponse> toChatMessageResponseList(List<ChatMessageRequest> messages);
}

