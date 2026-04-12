package ojt.aws.educare.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.configuration.CurrentUserProvider;
import ojt.aws.educare.dto.request.SaveDraftRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.QuizDraftResponse;
import ojt.aws.educare.entity.QuizDraft;
import ojt.aws.educare.entity.Submission;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.QuizDraftMapper;
import ojt.aws.educare.repository.QuizDraftRepository;
import ojt.aws.educare.repository.SubmissionRepository;
import ojt.aws.educare.service.QuizDraftService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizDraftServiceImpl implements QuizDraftService {

    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";

    SubmissionRepository submissionRepository;
    QuizDraftRepository quizDraftRepository;
    CurrentUserProvider currentUserProvider;
    ObjectMapper objectMapper;
    QuizDraftMapper quizDraftMapper;


    private User getCurrentUser() {
        return currentUserProvider.getCurrentUser();
    }


    private String serializeAnswers(List<SaveDraftRequest.AnswerItem> answers) {
        try {
            return objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<QuizDraftResponse.SavedAnswer> deserializeAnswers(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            List<Map<String, Object>> raw = objectMapper.readValue(
                    json, new TypeReference<>() {}
            );
            return raw.stream()
                    .map(m -> {
                        Integer qId = m.get("questionId") instanceof Number n ? n.intValue() : null;
                        Object aRaw = m.get("answerRefId");
                        Integer aId = aRaw instanceof Number n ? n.intValue() : null;
                        return quizDraftMapper.toSavedAnswer(qId, aId);
                    })
                    .toList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private QuizDraftResponse toResponse(QuizDraft draft) {
        return quizDraftMapper.toResponse(draft, deserializeAnswers(draft.getAnswersJson()));
    }


    @Override
    @Transactional
    public ApiResponse<QuizDraftResponse> saveDraft(Integer assignmentId, SaveDraftRequest request) {
        User currentUser = getCurrentUser();

        // Only allow saving drafts for submissions that are still in progress.
        Submission submission = submissionRepository
                .findByAssignment_AssignmentIDAndUser_UserID(assignmentId, currentUser.getUserID())
                .filter(s -> STATUS_IN_PROGRESS.equals(s.getSubmissionStatus()))
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_ATTEMPT_NOT_STARTED));

        String answersJson = serializeAnswers(request.getAnswers());

        // Upsert: update existing draft or create a new one
        QuizDraft draft = quizDraftRepository
                .findBySubmission_SubmissionID(submission.getSubmissionID())
                .orElseGet(() -> QuizDraft.builder().submission(submission).build());

        draft.setAnswersJson(answersJson);
        draft.setCurrentQuestion(request.getCurrentQuestion());
        draft.setLastSavedAt(LocalDateTime.now());

        quizDraftRepository.save(draft);

        return ApiResponse.success("Đã lưu bản nháp", toResponse(draft));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<QuizDraftResponse> getDraft(Integer assignmentId) {
        User currentUser = getCurrentUser();

        // Return 404 if the submission is no longer active — stale draft is invalid.
        Submission submission = submissionRepository
                .findByAssignment_AssignmentIDAndUser_UserID(assignmentId, currentUser.getUserID())
                .filter(s -> STATUS_IN_PROGRESS.equals(s.getSubmissionStatus()))
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_DRAFT_NOT_FOUND));

        QuizDraft draft = quizDraftRepository
                .findBySubmission_SubmissionID(submission.getSubmissionID())
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_DRAFT_NOT_FOUND));

        return ApiResponse.success("Lấy bản nháp thành công", toResponse(draft));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteDraft(Integer assignmentId) {
        User currentUser = getCurrentUser();

        submissionRepository
                .findByAssignment_AssignmentIDAndUser_UserID(assignmentId, currentUser.getUserID())
                .ifPresent(s -> quizDraftRepository.deleteBySubmission_SubmissionID(s.getSubmissionID()));

        return ApiResponse.success("Đã xóa bản nháp", null);
    }
}
