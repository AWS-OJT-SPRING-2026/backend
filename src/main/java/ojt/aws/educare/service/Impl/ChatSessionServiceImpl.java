package ojt.aws.educare.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.ChatMessageRequest;
import ojt.aws.educare.dto.request.ChatSessionUpsertRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.ChatMessageResponse;
import ojt.aws.educare.dto.response.ChatSessionResponse;
import ojt.aws.educare.entity.AIChatSession;
import ojt.aws.educare.entity.Student;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.ChatSessionMapper;
import ojt.aws.educare.repository.AIChatSessionRepository;
import ojt.aws.educare.repository.StudentRepository;
import ojt.aws.educare.repository.UserRepository;
import ojt.aws.educare.service.ChatSessionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatSessionServiceImpl implements ChatSessionService {
    AIChatSessionRepository aiChatSessionRepository;
    UserRepository userRepository;
    StudentRepository studentRepository;
    ChatSessionMapper chatSessionMapper;
    ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ChatSessionResponse>> getMySessions() {
        Student currentStudent = getCurrentStudent();

        List<AIChatSession> sessions = aiChatSessionRepository
                .findByStudent_StudentIDOrderByUpdatedAtDesc(currentStudent.getStudentID());

        List<ChatSessionResponse> responses = sessions.stream()
                .map(this::toChatSessionResponse)
                .toList();

        return ApiResponse.success("Lấy lịch sử chat thành công", responses);
    }

    @Override
    @Transactional
    public ApiResponse<ChatSessionResponse> upsertMySession(ChatSessionUpsertRequest request) {
        Student currentStudent = getCurrentStudent();

        String sessionKey = safeTrim(request.getSessionId());
        if (sessionKey.isEmpty()) {
            throw new AppException(ErrorCode.CHAT_SESSION_ID_REQUIRED);
        }

        String title = safeTrim(request.getTitle());
        if (title.isEmpty()) {
            title = deriveFallbackTitle(request.getMessages());
        }
        if (title.isEmpty()) {
            throw new AppException(ErrorCode.CHAT_SESSION_TITLE_REQUIRED);
        }

        List<ChatMessageRequest> messages = request.getMessages() == null
                ? new ArrayList<>()
                : request.getMessages();

        AIChatSession session = aiChatSessionRepository
                .findByStudent_StudentIDAndSessionKey(currentStudent.getStudentID(), sessionKey)
                .orElseGet(() -> AIChatSession.builder()
                        .student(currentStudent)
                        .sessionKey(sessionKey)
                        .build());

        session.setTitle(title);
        session.setMessagesJson(writeMessages(messages));

        AIChatSession savedSession = aiChatSessionRepository.save(session);
        return ApiResponse.success("Lưu lịch sử chat thành công", toChatSessionResponse(savedSession));
    }

    private ChatSessionResponse toChatSessionResponse(AIChatSession session) {
        List<ChatMessageRequest> messages = readMessages(session.getMessagesJson());
        List<ChatMessageResponse> messageResponses = chatSessionMapper.toChatMessageResponseList(messages);

        return ChatSessionResponse.builder()
                .sessionId(session.getSessionKey())
                .title(session.getTitle())
                .messages(messageResponses)
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    private String writeMessages(List<ChatMessageRequest> messages) {
        try {
            return objectMapper.writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.CHAT_SESSION_PROCESSING_ERROR);
        }
    }

    private List<ChatMessageRequest> readMessages(String messagesJson) {
        if (messagesJson == null || messagesJson.isBlank()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(messagesJson, new TypeReference<List<ChatMessageRequest>>() {
            });
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.CHAT_SESSION_PROCESSING_ERROR);
        }
    }

    private Student getCurrentStudent() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return studentRepository.findByUser_UserID(currentUser.getUserID())
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String deriveFallbackTitle(List<ChatMessageRequest> messages) {
        if (messages == null || messages.isEmpty()) {
            return "Cuoc hoi thoai moi";
        }

        return messages.stream()
                .filter(message -> message != null && "user".equalsIgnoreCase(message.getRole()))
                .map(ChatMessageRequest::getContent)
                .map(this::safeTrim)
                .filter(content -> !content.isEmpty())
                .findFirst()
                .map(this::truncateTitle)
                .orElse("Cuoc hoi thoai moi");
    }

    private String truncateTitle(String title) {
        int maxLength = 80;
        return title.length() <= maxLength ? title : title.substring(0, maxLength);
    }
}

