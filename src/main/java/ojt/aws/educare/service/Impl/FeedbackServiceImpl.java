package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.configuration.CurrentUserProvider;
import ojt.aws.educare.dto.request.FeedbackRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.FeedbackResponse;
import ojt.aws.educare.entity.*;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.FeedbackMapper;
import ojt.aws.educare.repository.AssignmentRepository;
import ojt.aws.educare.repository.FeedbackRepository;
import ojt.aws.educare.repository.StudentRepository;
import ojt.aws.educare.repository.TeacherRepository;
import ojt.aws.educare.service.FeedbackService;
import ojt.aws.educare.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackServiceImpl implements FeedbackService {

    FeedbackRepository feedbackRepository;
    StudentRepository studentRepository;
    AssignmentRepository assignmentRepository;
    TeacherRepository teacherRepository;
    FeedbackMapper feedbackMapper;
    NotificationService notificationService;
    CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ApiResponse<FeedbackResponse> createFeedback(FeedbackRequest request) {
        User currentUser = currentUserProvider.getCurrentUser();
        Teacher teacher = teacherRepository.findByUser_UserID(currentUser.getUserID())
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));

        Student student = studentRepository.findByUser_UserID(request.getStudentId())
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        Feedback feedback = feedbackMapper.toFeedback(request, student, assignment, teacher);

        Feedback saved = feedbackRepository.save(feedback);

        // Notify student
        String actionUrl = "/student/tests/" + assignment.getAssignmentID();
        notificationService.createNotification(
                student.getUser(),
                NotificationType.FEEDBACK_RECEIVED,
                "Bạn nhận được nhận xét từ giáo viên",
                "Bài làm của bạn trong \"" + assignment.getTitle() + "\" đã được giáo viên nhận xét",
                actionUrl
        );

        return ApiResponse.<FeedbackResponse>builder()
                .result(feedbackMapper.toResponse(saved))
                .message("Đã gửi nhận xét thành công")
                .build();
    }

    @Override
    public ApiResponse<List<FeedbackResponse>> getFeedbackByStudent(Integer studentId) {
        Student student = studentRepository.findByUser_UserID(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        List<FeedbackResponse> result = feedbackRepository
                .findByStudent_StudentIDOrderByCreatedAtDesc(student.getStudentID())
                .stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.<List<FeedbackResponse>>builder().result(result).build();
    }

    @Override
    public ApiResponse<List<FeedbackResponse>> getMyFeedbackForAssignment(Integer assignmentId) {
        User currentUser = currentUserProvider.getCurrentUser();
        Student student = studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        List<FeedbackResponse> result = feedbackRepository
                .findByAssignment_AssignmentIDAndStudent_StudentID(assignmentId, student.getStudentID())
                .stream()
                .map(feedbackMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.<List<FeedbackResponse>>builder().result(result).build();
    }
}
