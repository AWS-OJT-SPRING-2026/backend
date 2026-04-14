package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.configuration.CurrentUserProvider;
import ojt.aws.educare.dto.request.AnswerRequest;
import ojt.aws.educare.dto.request.QuestionCreateRequest;
import ojt.aws.educare.dto.request.QuestionUpdateRequest;
import ojt.aws.educare.dto.response.AnswerResponse;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.QuestionPreviewResponse;
import ojt.aws.educare.entity.Answer;
import ojt.aws.educare.entity.Question;
import ojt.aws.educare.entity.QuestionBank;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.QuestionMapper;
import ojt.aws.educare.repository.AnswerRepository;
import ojt.aws.educare.repository.QuestionBankRepository;
import ojt.aws.educare.repository.QuestionRepository;
import ojt.aws.educare.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuestionServiceImpl implements QuestionService {

    QuestionRepository questionRepository;
    AnswerRepository answerRepository;
    QuestionBankRepository questionBankRepository;
    QuestionMapper questionMapper;
    CurrentUserProvider currentUserProvider;

    @Override
    public ApiResponse<List<QuestionPreviewResponse>> getQuestionsByBankId(Integer bankId) {
        // Teacher has to own the question bank
        User currentUser = currentUserProvider.getCurrentUser();
        QuestionBank bank = questionBankRepository.findById(bankId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND)); // Or bank not found

        // Get all questions associated with any bank having the same name in the system
        List<Question> questions = questionRepository.findByBankName(bank.getBankName());
        List<QuestionPreviewResponse> responses = new ArrayList<>();

        for (Question q : questions) {
            List<Answer> answers = answerRepository.findByQuestion_Id(q.getId());
            List<AnswerResponse> answerResponses = answers.stream().map(this::mapToAnswerResponse).collect(Collectors.toList());
            responses.add(questionMapper.toPreviewResponse(q, answerResponses));
        }

        return ApiResponse.success("Lấy danh sách câu hỏi thành công", responses);
    }

    @Override
    @Transactional
    public ApiResponse<QuestionPreviewResponse> createQuestion(Integer bankId, QuestionCreateRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        QuestionBank bank = questionBankRepository.findById(bankId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        if (!bank.getUser().getUserID().equals(currentUser.getUserID())) {
            throw new AppException(ErrorCode.NO_PERMISSION_DISTRIBUTE_DOCUMENT);
        }

        Question question = Question.builder()
                .questionText(request.getQuestionText())
                .imageUrl(request.getImageUrl())
                .explanation(request.getExplanation())
                .difficultyLevel(request.getDifficultyLevel())
                .isAi(false)
                .bank(bank)
                .build();

        Question savedQuestion = questionRepository.save(question);

        List<Answer> newAnswers = new ArrayList<>();
        if (request.getAnswers() != null) {
            for (AnswerRequest ar : request.getAnswers()) {
                Answer a = Answer.builder()
                        .label(ar.getLabel())
                        .content(ar.getContent())
                        .isCorrect(ar.getIsCorrect())
                        .question(savedQuestion)
                        .build();
                newAnswers.add(a);
            }
        }
        answerRepository.saveAll(newAnswers);

        List<AnswerResponse> answerResponses = newAnswers.stream().map(this::mapToAnswerResponse).collect(Collectors.toList());
        return ApiResponse.success("Tạo câu hỏi thành công", questionMapper.toPreviewResponse(savedQuestion, answerResponses));
    }

    @Override
    @Transactional
    public ApiResponse<QuestionPreviewResponse> updateQuestion(Integer questionId, QuestionUpdateRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND)); // Not found question

        if (!question.getBank().getUser().getUserID().equals(currentUser.getUserID())) {
            throw new AppException(ErrorCode.NO_PERMISSION_DISTRIBUTE_DOCUMENT); 
        }

        question.setQuestionText(request.getQuestionText());
        question.setImageUrl(request.getImageUrl());
        question.setExplanation(request.getExplanation());
        question.setDifficultyLevel(request.getDifficultyLevel());

        Question updatedQuestion = questionRepository.save(question);

        // Update answers: delete old missing ones, update existing, add new ones
        List<Answer> existingAnswers = answerRepository.findByQuestion_Id(questionId);
        
        // Remove answers not in request
        List<Integer> reqIds = request.getAnswers().stream()
                .filter(a -> a.getId() != null)
                .map(AnswerRequest::getId)
                .collect(Collectors.toList());

        List<Answer> answersToDelete = existingAnswers.stream()
                .filter(a -> !reqIds.contains(a.getId()))
                .collect(Collectors.toList());
        
        if (!answersToDelete.isEmpty()) {
            answerRepository.deleteAll(answersToDelete);
        }

        List<Answer> updatedAnswers = new ArrayList<>();
        for (AnswerRequest reqAnswer : request.getAnswers()) {
            if (reqAnswer.getId() != null) {
                // Update
                Answer existing = existingAnswers.stream()
                        .filter(a -> a.getId().equals(reqAnswer.getId()))
                        .findFirst()
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST)); // Not found answer ID
                existing.setLabel(reqAnswer.getLabel());
                existing.setContent(reqAnswer.getContent());
                existing.setIsCorrect(reqAnswer.getIsCorrect());
                updatedAnswers.add(existing);
            } else {
                // Create new
                Answer newAnswer = Answer.builder()
                        .label(reqAnswer.getLabel())
                        .content(reqAnswer.getContent())
                        .isCorrect(reqAnswer.getIsCorrect())
                        .question(updatedQuestion)
                        .build();
                updatedAnswers.add(newAnswer);
            }
        }
        answerRepository.saveAll(updatedAnswers);

        List<AnswerResponse> answerResponses = updatedAnswers.stream().map(this::mapToAnswerResponse).collect(Collectors.toList());
        return ApiResponse.success("Cập nhật câu hỏi thành công", questionMapper.toPreviewResponse(updatedQuestion, answerResponses));
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteQuestion(Integer questionId) {
        User currentUser = currentUserProvider.getCurrentUser();
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        if (!question.getBank().getUser().getUserID().equals(currentUser.getUserID())) {
            throw new AppException(ErrorCode.NO_PERMISSION_DISTRIBUTE_DOCUMENT); 
        }

        List<Answer> existingAnswers = answerRepository.findByQuestion_Id(questionId);
        if (!existingAnswers.isEmpty()) {
            answerRepository.deleteAll(existingAnswers);
        }

        questionRepository.delete(question);

        return ApiResponse.success("Xóa câu hỏi thành công", null);
    }

    private AnswerResponse mapToAnswerResponse(Answer answer) {
        return AnswerResponse.builder()
                .id(answer.getId())
                .label(answer.getLabel())
                .content(answer.getContent())
                .isCorrect(answer.getIsCorrect())
                .build();
    }
}
