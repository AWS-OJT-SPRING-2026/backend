package ojt.aws.educare.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.ChatSessionUpsertRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.ChatSessionResponse;
import ojt.aws.educare.service.ChatSessionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat/sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatSessionController {
	ChatSessionService chatSessionService;

	@GetMapping
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<List<ChatSessionResponse>> getMySessions() {
		return chatSessionService.getMySessions();
	}

	@PostMapping
	@PreAuthorize("hasRole('STUDENT')")
	public ApiResponse<ChatSessionResponse> upsertMySession(@RequestBody ChatSessionUpsertRequest request) {
		return chatSessionService.upsertMySession(request);
	}
}

