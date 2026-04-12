package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.ChatMessageRequest;
import ojt.aws.educare.dto.response.ChatMessageResponse;
import ojt.aws.educare.dto.response.ChatSessionResponse;
import ojt.aws.educare.entity.AIChatSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatSessionMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessageRequest message);

    List<ChatMessageResponse> toChatMessageResponseList(List<ChatMessageRequest> messages);

    @Mapping(target = "sessionId", source = "session.sessionKey")
    @Mapping(target = "title", source = "session.title")
    @Mapping(target = "messages", source = "messages")
    @Mapping(target = "createdAt", source = "session.createdAt")
    @Mapping(target = "updatedAt", source = "session.updatedAt")
    ChatSessionResponse toChatSessionResponse(AIChatSession session, List<ChatMessageResponse> messages);
}

