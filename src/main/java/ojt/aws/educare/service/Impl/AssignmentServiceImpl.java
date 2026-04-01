package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.CreateAssignmentRequest;
import ojt.aws.educare.dto.request.SubmitAssignmentRequest;
import ojt.aws.educare.dto.request.UpdateAssignmentRequest;
import ojt.aws.educare.dto.response.*;
import ojt.aws.educare.entity.*;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.AssignmentMapper;
import ojt.aws.educare.mapper.QuestionMapper;
import ojt.aws.educare.mapper.SubmissionMapper;
import ojt.aws.educare.repository.*;
import ojt.aws.educare.service.AssignmentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AssignmentServiceImpl implements AssignmentService {

    private static final String ASSIGNMENT_TYPE_TEST = "TEST";
    private static final String ASSIGNMENT_TYPE_ASSIGNMENT = "ASSIGNMENT";
    private static final String FORMAT_MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
    private static final String FORMAT_ESSAY = "ESSAY";

    AssignmentRepository assignmentRepository;
    QuestionRepository questionRepository;
    SubmissionRepository submissionRepository;
    SubmissionAnswerRepository submissionAnswerRepository;
    AnswerRepository answerRepository;
    QuestionBankRepository questionBankRepository;
    ClassroomRepository classroomRepository;
    ClassMemberRepository classMemberRepository;
    StudentRepository studentRepository;
    TeacherRepository teacherRepository;
    UserRepository userRepository;
    AssignmentMapper assignmentMapper;
    QuestionMapper questionMapper;
    SubmissionMapper submissionMapper;

    private List<SubmissionResponse.SubmissionAnswerDetail> buildSubmissionAnswerDetails(List<SubmissionAnswer> answers) {
        return answers.stream().map(submissionMapper::toSubmissionAnswerDetail).toList();
    }

    private SubmissionResponse buildSubmissionResponse(Submission submission, List<SubmissionAnswer> answers) {
        return submissionMapper.toSubmissionResponse(submission, buildSubmissionAnswerDetails(answers));
    }

    private Map<Integer, Answer> mapCorrectAnswersByQuestionIds(List<Integer> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return answerRepository.findByQuestion_IdIn(questionIds).stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getQuestion().getId(),
                        a -> a,
                        (existing, ignored) -> existing
                ));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Teacher getCurrentTeacher(User currentUser) {
        return teacherRepository.findByUser(currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));
    }

    private String normalizeAssignmentType(String raw) {
        if (raw == null) {
            throw new AppException(ErrorCode.ASSIGNMENT_TYPE_INVALID);
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (!ASSIGNMENT_TYPE_TEST.equals(normalized) && !ASSIGNMENT_TYPE_ASSIGNMENT.equals(normalized)) {
            throw new AppException(ErrorCode.ASSIGNMENT_TYPE_INVALID);
        }
        return normalized;
    }

    private String normalizeFormat(String raw) {
        if (raw == null) {
            throw new AppException(ErrorCode.ASSIGNMENT_FORMAT_INVALID);
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if (!FORMAT_MULTIPLE_CHOICE.equals(normalized) && !FORMAT_ESSAY.equals(normalized)) {
            throw new AppException(ErrorCode.ASSIGNMENT_FORMAT_INVALID);
        }
        return normalized;
    }

    private void validateTimeByAssignmentType(
            String assignmentType,
            LocalDateTime startTime,
            LocalDateTime endTime,
            LocalDateTime deadline,
            Integer durationMinutes
    ) {
        if (durationMinutes == null) {
            throw new AppException(ErrorCode.ASSIGNMENT_DURATION_REQUIRED);
        }
        if (durationMinutes <= 0) {
            throw new AppException(ErrorCode.ASSIGNMENT_DURATION_INVALID);
        }

        if (ASSIGNMENT_TYPE_TEST.equals(assignmentType)) {
            if (startTime == null || endTime == null) {
                throw new AppException(ErrorCode.ASSIGNMENT_TEST_TIME_REQUIRED);
            }
            if (!endTime.isAfter(startTime)) {
                throw new AppException(ErrorCode.ASSIGNMENT_TEST_TIME_INVALID);
            }
            return;
        }

        if (deadline == null) {
            throw new AppException(ErrorCode.ASSIGNMENT_DEADLINE_REQUIRED);
        }
        if (!deadline.isAfter(LocalDateTime.now())) {
            throw new AppException(ErrorCode.ASSIGNMENT_DEADLINE_INVALID);
        }
    }

    private void applyTimeByAssignmentType(
            Assignment assignment,
            String assignmentType,
            LocalDateTime startTime,
            LocalDateTime endTime,
            LocalDateTime deadline,
            Integer durationMinutes
    ) {
        assignment.setDurationMinutes(durationMinutes);

        if (ASSIGNMENT_TYPE_TEST.equals(assignmentType)) {
            assignment.setStartTime(startTime);
            assignment.setEndTime(endTime);
            assignment.setDeadline(null);
            return;
        }

        assignment.setStartTime(null);
        assignment.setEndTime(null);
        assignment.setDeadline(deadline);
    }

    private void validateAssignmentCanBeStarted(Assignment assignment, LocalDateTime now) {
        if (!"ACTIVE".equals(assignment.getStatus())) {
            throw new AppException(ErrorCode.ASSIGNMENT_NOT_ACTIVE);
        }

        if (ASSIGNMENT_TYPE_TEST.equals(assignment.getAssignmentType())) {
            if (assignment.getStartTime() != null && now.isBefore(assignment.getStartTime())) {
                throw new AppException(ErrorCode.ASSIGNMENT_TEST_NOT_STARTED);
            }
            if (assignment.getEndTime() != null && !now.isBefore(assignment.getEndTime())) {
                throw new AppException(ErrorCode.ASSIGNMENT_TEST_ENDED);
            }
            return;
        }

        if (assignment.getDeadline() != null && !now.isBefore(assignment.getDeadline())) {
            throw new AppException(ErrorCode.ASSIGNMENT_DEADLINE_PASSED);
        }
    }

    private LocalDateTime calculateExpiredAt(Assignment assignment, LocalDateTime startedAt) {
        if (assignment.getDurationMinutes() == null) {
            throw new AppException(ErrorCode.ASSIGNMENT_DURATION_REQUIRED);
        }
        if (assignment.getDurationMinutes() <= 0) {
            throw new AppException(ErrorCode.ASSIGNMENT_DURATION_INVALID);
        }

        LocalDateTime calculatedEnd = startedAt.plusMinutes(assignment.getDurationMinutes());
        LocalDateTime hardEnd = ASSIGNMENT_TYPE_TEST.equals(assignment.getAssignmentType())
                ? assignment.getEndTime()
                : assignment.getDeadline();

        if (hardEnd == null) {
            return calculatedEnd;
        }
        return calculatedEnd.isBefore(hardEnd) ? calculatedEnd : hardEnd;
    }

    private Map<Integer, List<AnswerResponse>> mapAnswersByQuestionId(List<Integer> questionIds) {
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return answerRepository.findByQuestion_IdIn(questionIds).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        a -> a.getQuestion().getId(),
                        java.util.stream.Collectors.mapping(
                                a -> AnswerResponse.builder()
                                        .id(a.getId())
                                        .label(a.getLabel())
                                        .content(a.getContent())
                                        .isCorrect(a.getIsCorrect())
                                        .build(),
                                java.util.stream.Collectors.toList()
                        )
                ));
    }

    private List<QuestionPreviewResponse> buildQuestionPreviewsByIds(List<Integer> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<QuestionRepository.QuestionRandomProjection> projections =
                questionRepository.findQuestionPreviewDataByIds(questionIds);
        Map<Integer, QuestionRepository.QuestionRandomProjection> projectionMap = projections.stream()
                .collect(java.util.stream.Collectors.toMap(
                        QuestionRepository.QuestionRandomProjection::getId,
                        p -> p
                ));

        Map<Integer, List<AnswerResponse>> answersByQuestionId = mapAnswersByQuestionId(questionIds);

        return questionIds.stream()
                .map(projectionMap::get)
                .filter(Objects::nonNull)
                .map(p -> questionMapper.toPreviewResponse(
                        p,
                        answersByQuestionId.getOrDefault(p.getId(), Collections.emptyList())
                ))
                .toList();
    }

    private List<Integer> normalizeQuestionIds(List<Integer> questionIds) {
        if (questionIds == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(new LinkedHashSet<>(questionIds));
    }

    private void validateQuestionIdsExist(List<Integer> questionIds) {
        for (Integer qId : questionIds) {
            if (!questionRepository.existsById(qId)) {
                throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
            }
        }
    }

    private void replaceAssignmentQuestionLinks(Integer assignmentId, List<Integer> questionIds) {
        assignmentRepository.deleteAssignmentQuestionsByAssignmentId(assignmentId);
        for (Integer qId : questionIds) {
            assignmentRepository.insertAssignmentQuestion(assignmentId, qId);
        }
    }

    private AssignmentResponse mapAssignmentResponseWithCounts(Assignment assignment) {
        int totalQuestions = (int) assignmentRepository.countQuestionsByAssignmentId(assignment.getAssignmentID());
        int totalSubmissions = (int) submissionRepository.countByAssignment_AssignmentID(assignment.getAssignmentID());
        return assignmentMapper.toResponse(assignment, totalQuestions, totalSubmissions);
    }

    private AssignmentDetailResponse mapAssignmentDetailWithCounts(
            Assignment assignment,
            List<QuestionPreviewResponse> questionPreviews
    ) {
        int totalSubmissions = (int) submissionRepository.countByAssignment_AssignmentID(assignment.getAssignmentID());
        return assignmentMapper.toDetailResponse(assignment, questionPreviews, totalSubmissions);
    }

    @Override
    @Transactional
    public ApiResponse<AssignmentDetailResponse> createAssignment(CreateAssignmentRequest request) {
        User currentUser = getCurrentUser();
        Teacher currentTeacher = getCurrentTeacher(currentUser);
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        List<Integer> questionIds = normalizeQuestionIds(request.getQuestionIds());
        validateQuestionIdsExist(questionIds);

        String assignmentType = normalizeAssignmentType(request.getAssignmentType());
        String format = normalizeFormat(request.getFormat());
        validateTimeByAssignmentType(assignmentType, request.getStartTime(), request.getEndTime(), request.getDeadline(), request.getDurationMinutes());

        Assignment assignment = Assignment.builder()
                .classroom(classroom)
                .user(currentUser)
                .teacher(currentTeacher)
                .title(request.getTitle())
                .assignmentType(assignmentType)
                .format(format)
                .status("DRAFT")
                .build();

        applyTimeByAssignmentType(assignment, assignmentType, request.getStartTime(), request.getEndTime(), request.getDeadline(), request.getDurationMinutes());

        assignment = assignmentRepository.save(assignment);
        replaceAssignmentQuestionLinks(assignment.getAssignmentID(), questionIds);

        List<QuestionPreviewResponse> questionPreviews = buildQuestionPreviewsByIds(questionIds);

        return ApiResponse.success("Tạo đề kiểm tra thành công",
                mapAssignmentDetailWithCounts(assignment, questionPreviews));
    }

    @Override
    @Transactional
    public ApiResponse<AssignmentDetailResponse> updateAssignment(Integer assignmentId, UpdateAssignmentRequest request) {
        User currentUser = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        if (!assignment.getUser().getUserID().equals(currentUser.getUserID())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        String assignmentType = request.getAssignmentType() != null
                ? normalizeAssignmentType(request.getAssignmentType())
                : normalizeAssignmentType(assignment.getAssignmentType());

        // Format is immutable after creation; keep existing value.
        String format = assignment.getFormat();

        LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : assignment.getStartTime();
        LocalDateTime endTime = request.getEndTime() != null ? request.getEndTime() : assignment.getEndTime();
        LocalDateTime deadline = request.getDeadline() != null ? request.getDeadline() : assignment.getDeadline();
        Integer durationMinutes = request.getDurationMinutes() != null
                ? request.getDurationMinutes()
                : assignment.getDurationMinutes();

        validateTimeByAssignmentType(assignmentType, startTime, endTime, deadline, durationMinutes);

        if (request.getTitle() != null) assignment.setTitle(request.getTitle());
        assignment.setAssignmentType(assignmentType);
        assignment.setFormat(format);
        applyTimeByAssignmentType(assignment, assignmentType, startTime, endTime, deadline, durationMinutes);

        if (request.getQuestionIds() != null) {
            List<Integer> questionIds = normalizeQuestionIds(request.getQuestionIds());
            validateQuestionIdsExist(questionIds);
            replaceAssignmentQuestionLinks(assignmentId, questionIds);
        }

        assignment = assignmentRepository.save(assignment);

        List<Integer> questionIds = request.getQuestionIds() != null
                ? request.getQuestionIds()
                : assignmentRepository.findQuestionIdsByAssignmentId(assignmentId);
        List<QuestionPreviewResponse> questionPreviews = buildQuestionPreviewsByIds(questionIds);

        return ApiResponse.success("Cập nhật đề kiểm tra thành công",
                mapAssignmentDetailWithCounts(assignment, questionPreviews));
    }

    @Override
    @Transactional
    public ApiResponse<AssignmentResponse> publishAssignment(Integer assignmentId) {
        User currentUser = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        if (!assignment.getUser().getUserID().equals(currentUser.getUserID())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        if (!"DRAFT".equals(assignment.getStatus())) {
            throw new AppException(ErrorCode.ASSIGNMENT_ALREADY_PUBLISHED);
        }
        if (assignmentRepository.countQuestionsByAssignmentId(assignmentId) == 0) {
            throw new AppException(ErrorCode.ASSIGNMENT_HAS_NO_QUESTIONS);
        }
        if (ASSIGNMENT_TYPE_ASSIGNMENT.equals(assignment.getAssignmentType())
                && assignment.getDeadline() != null
                && assignment.getDeadline().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.ASSIGNMENT_DEADLINE_PASSED);
        }

        assignment.setStatus("ACTIVE");
        assignment = assignmentRepository.save(assignment);

        return ApiResponse.success("Phát hành đề kiểm tra thành công", mapAssignmentResponseWithCounts(assignment));
    }

    @Override
    @Transactional
    public ApiResponse<AssignmentResponse> closeAssignment(Integer assignmentId) {
        User currentUser = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        if (!assignment.getUser().getUserID().equals(currentUser.getUserID())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        if (!"ACTIVE".equals(assignment.getStatus()) && !"CLOSED".equals(assignment.getStatus())) {
            throw new AppException(ErrorCode.ASSIGNMENT_NOT_ACTIVE);
        }

        boolean reopening = "CLOSED".equals(assignment.getStatus());
        assignment.setStatus(reopening ? "ACTIVE" : "CLOSED");
        assignment = assignmentRepository.save(assignment);

        return ApiResponse.success(
                reopening ? "Mở lại đề kiểm tra thành công" : "Đóng đề kiểm tra thành công",
                mapAssignmentResponseWithCounts(assignment)
        );
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteAssignment(Integer assignmentId) {
        User currentUser = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        if (!assignment.getUser().getUserID().equals(currentUser.getUserID())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        assignmentRepository.delete(assignment);
        return ApiResponse.success("Xóa đề kiểm tra thành công");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AssignmentResponse>> getMyAssignments() {
        User currentUser = getCurrentUser();
        List<Assignment> assignments = assignmentRepository.findByUser_UserIDOrderByCreatedAtDesc(currentUser.getUserID());
        List<AssignmentResponse> responses = assignments.stream()
                .map(this::mapAssignmentResponseWithCounts)
                .toList();
        return ApiResponse.success("Lấy danh sách đề kiểm tra thành công", responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AssignmentDetailResponse> getAssignmentDetail(Integer assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        List<Integer> questionIds = assignmentRepository.findQuestionIdsByAssignmentId(assignmentId);
        List<QuestionPreviewResponse> questionPreviews = buildQuestionPreviewsByIds(questionIds);

        return ApiResponse.success("Lấy chi tiết đề kiểm tra thành công",
                mapAssignmentDetailWithCounts(assignment, questionPreviews));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<QuestionPreviewResponse>> getRandomQuestions(Integer bankId, Integer difficultyLevel, Integer limit) {
        if (difficultyLevel != null && (difficultyLevel < 1 || difficultyLevel > 3)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        User currentUser = getCurrentUser();
        if (bankId != null) {
            QuestionBank bank = questionBankRepository.findById(bankId)
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_BANK_NOT_FOUND));

            if (!Objects.equals(bank.getUser().getUserID(), currentUser.getUserID())) {
                throw new AppException(ErrorCode.FORBIDDEN);
            }
        }

        List<QuestionRepository.QuestionRandomProjection> allQuestions =
                questionRepository.findRandomQuestionPreviewData(bankId, difficultyLevel);
        List<QuestionRepository.QuestionRandomProjection> mutable = new ArrayList<>(allQuestions);
        Collections.shuffle(mutable);

        int count = (limit != null && limit > 0) ? limit : 10;

        List<QuestionRepository.QuestionRandomProjection> selected = mutable.stream()
                .limit(count)
                .toList();

        List<Integer> questionIds = selected.stream()
                .map(QuestionRepository.QuestionRandomProjection::getId)
                .toList();
        Map<Integer, List<AnswerResponse>> answersByQuestionId = mapAnswersByQuestionId(questionIds);

        List<QuestionPreviewResponse> result = selected.stream()
                .map(q -> questionMapper.toPreviewResponse(
                        q,
                        answersByQuestionId.getOrDefault(q.getId(), Collections.emptyList())
                ))
                .toList();

        return ApiResponse.success("Lấy câu hỏi ngẫu nhiên thành công", result);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AssignmentReportResponse> getAssignmentReport(Integer assignmentId) {
        User currentUser = getCurrentUser();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        if (!assignment.getUser().getUserID().equals(currentUser.getUserID())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        List<Submission> submissions = submissionRepository.findByAssignment_AssignmentID(assignmentId);

        BigDecimal avgScore = BigDecimal.ZERO;
        BigDecimal highScore = BigDecimal.ZERO;
        BigDecimal lowScore = BigDecimal.ZERO;

        if (!submissions.isEmpty()) {
            List<BigDecimal> scores = submissions.stream()
                    .map(Submission::getScore)
                    .filter(Objects::nonNull)
                    .toList();

            if (!scores.isEmpty()) {
                BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                avgScore = sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
                highScore = scores.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
                lowScore = scores.stream().min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
            }
        }

        List<AssignmentReportResponse.StudentSubmissionSummary> studentResults = submissions.stream()
                .map(s -> AssignmentReportResponse.StudentSubmissionSummary.builder()
                        .submissionId(s.getSubmissionID())
                        .userId(s.getUser().getUserID())
                        .studentName(s.getUser().getFullName())
                        .score(s.getScore())
                        .timeTaken(s.getTimeTaken())
                        .submitTime(s.getSubmittedAt())
                        .build())
                .toList();

        List<Object[]> rawStats = submissionAnswerRepository.countCorrectByQuestionForAssignment(assignmentId);
        List<AssignmentReportResponse.QuestionStatistic> questionStats = rawStats.stream()
                .map(row -> {
                    Integer questionId = (Integer) row[0];
                    long total = ((Number) row[1]).longValue();
                    long correct = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                    Question q = questionRepository.findById(questionId).orElse(null);
                    double accuracy = total > 0 ? Math.round(correct * 10000.0 / total) / 100.0 : 0.0;
                    return AssignmentReportResponse.QuestionStatistic.builder()
                            .questionId(questionId)
                            .questionText(q != null ? q.getQuestionText() : "")
                            .difficultyLevel(q != null ? q.getDifficultyLevel() : null)
                            .correctCount((int) correct)
                            .totalAnswered((int) total)
                            .accuracyRate(accuracy)
                            .build();
                })
                .toList();

        AssignmentReportResponse report = AssignmentReportResponse.builder()
                .assignmentId(assignmentId)
                .title(assignment.getTitle())
                .className(assignment.getClassroom() != null ? assignment.getClassroom().getClassName() : null)
                .totalSubmissions(submissions.size())
                .averageScore(avgScore)
                .highestScore(highScore)
                .lowestScore(lowScore)
                .studentResults(studentResults)
                .questionStats(questionStats)
                .build();

        return ApiResponse.success("Lấy báo cáo đề kiểm tra thành công", report);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AssignmentResponse>> getAssignmentsForStudent(Integer classroomId) {
        List<Assignment> assignments = assignmentRepository.findByClassroom_ClassIDAndStatus(classroomId, "ACTIVE");
        List<AssignmentResponse> responses = assignments.stream()
                .map(this::mapAssignmentResponseWithCounts)
                .toList();
        return ApiResponse.success("Lấy danh sách đề kiểm tra thành công", responses);
    }

    @Override
    @Transactional
    public ApiResponse<AssignmentAttemptResponse> startAssignment(Integer assignmentId) {
        User currentUser = getCurrentUser();

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        validateAssignmentCanBeStarted(assignment, now);

        Submission existing = submissionRepository
                .findByAssignment_AssignmentIDAndUser_UserID(assignmentId, currentUser.getUserID())
                .orElse(null);

        if (existing != null) {
            if (existing.getSubmittedAt() != null) {
                throw new AppException(ErrorCode.ALREADY_SUBMITTED);
            }
            if (existing.getExpiredAt() != null && now.isAfter(existing.getExpiredAt())) {
                throw new AppException(ErrorCode.ASSIGNMENT_ATTEMPT_EXPIRED);
            }

            return ApiResponse.success("Bắt đầu làm bài thành công",
                    AssignmentAttemptResponse.builder()
                            .submissionID(existing.getSubmissionID())
                            .assignmentId(assignmentId)
                            .startedAt(existing.getStartedAt())
                            .expiredAt(existing.getExpiredAt())
                            .build());
        }

        LocalDateTime expiredAt = calculateExpiredAt(assignment, now);
        Submission created = submissionRepository.save(Submission.builder()
                .assignment(assignment)
                .user(currentUser)
                .startedAt(now)
                .expiredAt(expiredAt)
                .build());

        return ApiResponse.success("Bắt đầu làm bài thành công",
                AssignmentAttemptResponse.builder()
                        .submissionID(created.getSubmissionID())
                        .assignmentId(assignmentId)
                        .startedAt(created.getStartedAt())
                        .expiredAt(created.getExpiredAt())
                        .build());
    }

    @Override
    @Transactional
    public ApiResponse<SubmissionResponse> submitAssignment(Integer assignmentId, SubmitAssignmentRequest request) {
        User currentUser = getCurrentUser();

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        Submission submission = submissionRepository
                .findWithLockByAssignment_AssignmentIDAndUser_UserID(assignmentId, currentUser.getUserID())
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_ATTEMPT_NOT_STARTED));

        if (submission.getSubmittedAt() != null) {
            throw new AppException(ErrorCode.ALREADY_SUBMITTED);
        }

        LocalDateTime now = LocalDateTime.now();
        if (submission.getExpiredAt() != null && now.isAfter(submission.getExpiredAt().plusSeconds(5))) {
            throw new AppException(ErrorCode.ASSIGNMENT_ATTEMPT_EXPIRED);
        }

        List<Integer> assignmentQuestionIds = assignmentRepository.findQuestionIdsByAssignmentId(assignmentId);
        Set<Integer> assignmentQuestionSet = new LinkedHashSet<>(assignmentQuestionIds);

        Map<Integer, SubmitAssignmentRequest.AnswerItem> answerByQuestionId = new LinkedHashMap<>();
        for (SubmitAssignmentRequest.AnswerItem item : request.getAnswers()) {
            if (!assignmentQuestionSet.contains(item.getQuestionId())) {
                throw new AppException(ErrorCode.ASSIGNMENT_QUESTION_NOT_FOUND);
            }
            answerByQuestionId.put(item.getQuestionId(), item);
        }

        int correctCount = 0;
        int totalCount = assignmentQuestionSet.size();
        List<SubmissionAnswer> submissionAnswers = new ArrayList<>();

        for (Integer questionId : assignmentQuestionSet) {
            SubmitAssignmentRequest.AnswerItem item = answerByQuestionId.get(questionId);
            if (item == null) {
                continue;
            }

            // Do not persist a blank row for unanswered questions.
            if (item.getAnswerRefId() == null
                    && (item.getSelectedAnswer() == null || item.getSelectedAnswer().isBlank())) {
                continue;
            }

            Question question = questionRepository.getReferenceById(questionId);

            Answer answerRef = null;
            boolean isCorrect = false;
            String selectedAnswer = item.getSelectedAnswer();

            if (item.getAnswerRefId() != null) {
                answerRef = answerRepository.findById(item.getAnswerRefId())
                        .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_ANSWER_NOT_MATCH_QUESTION));
                if (!answerRef.getQuestion().getId().equals(questionId)) {
                    throw new AppException(ErrorCode.ASSIGNMENT_ANSWER_NOT_MATCH_QUESTION);
                }
                selectedAnswer = answerRef.getContent();
                if (Boolean.TRUE.equals(answerRef.getIsCorrect())) {
                    isCorrect = true;
                    correctCount++;
                }
            }

            SubmissionAnswer sa = SubmissionAnswer.builder()
                    .submission(submission)
                    .question(question)
                    .answerRef(answerRef)
                    .selectedAnswer(selectedAnswer)
                    .isCorrect(isCorrect)
                    .build();
            submissionAnswers.add(sa);
        }

        submissionAnswerRepository.saveAll(submissionAnswers);

        BigDecimal score = totalCount > 0
                ? new BigDecimal(correctCount).multiply(BigDecimal.TEN)
                        .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        submission.setScore(score);
        submission.setTimeTaken(request.getTimeTaken());
        submission.setSubmittedAt(now);
        submissionRepository.save(submission);

        SubmissionResponse response = buildSubmissionResponse(submission, submissionAnswers);

        return ApiResponse.success("Nộp bài thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<SubmissionResponse> getMySubmission(Integer assignmentId) {
        User currentUser = getCurrentUser();

        Submission submission = submissionRepository
                .findByAssignment_AssignmentIDAndUser_UserID(assignmentId, currentUser.getUserID())
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        List<SubmissionAnswer> answers = submissionAnswerRepository
                .findBySubmission_SubmissionID(submission.getSubmissionID());

        SubmissionResponse response = buildSubmissionResponse(submission, answers);

        return ApiResponse.success("Lấy bài nộp thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<AssignmentResponse>> getStudentActiveAssignments() {
        User currentUser = getCurrentUser();
        Student student = studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        List<ClassMember> memberships = classMemberRepository.findByStudent_StudentID(student.getStudentID());
        List<Assignment> assignments = memberships.stream()
                .flatMap(m -> assignmentRepository.findByClassroom_ClassIDAndStatus(
                        m.getClassroom().getClassID(), "ACTIVE").stream())
                .distinct()
                .toList();

        List<AssignmentResponse> responses = assignments.stream()
                .map(this::mapAssignmentResponseWithCounts)
                .toList();

        return ApiResponse.success("Lấy danh sách đề kiểm tra thành công", responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<SubmissionResponse>> getStudentSubmissions() {
        User currentUser = getCurrentUser();

        List<Submission> submissions = submissionRepository.findByUser_UserID(currentUser.getUserID());

        List<SubmissionResponse> responses = submissions.stream()
                .map(s -> {
                    List<SubmissionAnswer> answers = submissionAnswerRepository
                            .findBySubmission_SubmissionID(s.getSubmissionID());
                    return buildSubmissionResponse(s, answers);
                })
                .toList();

        return ApiResponse.success("Lấy danh sách bài đã nộp thành công", responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AssignmentResultResponse> getMyResult(Integer assignmentId) {
        User currentUser = getCurrentUser();

        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        Submission submission = submissionRepository
                .findByAssignment_AssignmentIDAndUser_UserID(assignmentId, currentUser.getUserID())
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_RESULT_NOT_FOUND));

        if (submission.getSubmittedAt() == null) {
            throw new AppException(ErrorCode.ASSIGNMENT_RESULT_NOT_FOUND);
        }

        List<SubmissionAnswer> answers = submissionAnswerRepository
                .findBySubmission_SubmissionID(submission.getSubmissionID());
        List<Integer> assignmentQuestionIds = assignmentRepository.findQuestionIdsByAssignmentId(assignmentId);
        Map<Integer, Answer> correctAnswerByQuestion = mapCorrectAnswersByQuestionIds(assignmentQuestionIds);

        List<AssignmentResultResponse.QuestionResult> questionResults = answers.stream()
                .map(sa -> {
                    Answer correctAnswer = correctAnswerByQuestion.get(sa.getQuestion().getId());
                    return submissionMapper.toQuestionResult(
                            sa,
                            correctAnswer != null ? correctAnswer.getId() : null,
                            correctAnswer != null ? correctAnswer.getContent() : null
                    );
                })
                .toList();

        int correctCount = (int) answers.stream().filter(sa -> Boolean.TRUE.equals(sa.getIsCorrect())).count();

        AssignmentResultResponse response = submissionMapper.toAssignmentResultResponse(
                submission,
                assignmentQuestionIds.size(),
                correctCount,
                questionResults
        );

        return ApiResponse.success("Lấy kết quả bài làm thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<QuestionBankResponse>> getMyQuestionBanks() {
        User currentUser = getCurrentUser();
        List<QuestionBank> banks = questionBankRepository.findByUser_UserID(currentUser.getUserID());

        List<QuestionBankResponse> responses = banks.stream()
                .map(b -> QuestionBankResponse.builder()
                        .id(b.getId())
                        .bankName(b.getBankName())
                        .subjectId(b.getSubject() != null ? b.getSubject().getSubjectID() : null)
                        .subjectName(b.getSubject() != null ? b.getSubject().getSubjectName() : null)
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();

        return ApiResponse.success("Lấy danh sách ngân hàng câu hỏi thành công", responses);
    }
}
