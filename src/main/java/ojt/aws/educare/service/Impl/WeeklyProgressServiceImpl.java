package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.configuration.CurrentUserProvider;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.WeeklyProgressResponse;
import ojt.aws.educare.entity.Assignment;
import ojt.aws.educare.entity.Attendance;
import ojt.aws.educare.entity.Student;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.repository.*;
import ojt.aws.educare.service.WeeklyProgressService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WeeklyProgressServiceImpl implements WeeklyProgressService {

    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String TYPE_ASSIGNMENT = "ASSIGNMENT";
    private static final String TYPE_TEST = "TEST";

    CurrentUserProvider currentUserProvider;
    StudentRepository studentRepository;
    ClassMemberRepository classMemberRepository;
    AssignmentRepository assignmentRepository;
    SubmissionRepository submissionRepository;
    AttendanceRepository attendanceRepository;

    @Override
    public ApiResponse<WeeklyProgressResponse> getMyWeeklyProgress() {
        User currentUser = currentUserProvider.getCurrentUser();
        Student student = studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        // ISO week: Monday → Sunday
        LocalDate today = LocalDate.now();
        LocalDateTime weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime weekEnd = today.with(DayOfWeek.SUNDAY).atTime(23, 59, 59);

        // Classrooms the student belongs to
        List<Integer> classroomIds = classMemberRepository
                .findByStudent_StudentID(student.getStudentID())
                .stream()
                .map(cm -> cm.getClassroom().getClassID())
                .collect(Collectors.toList());

        if (classroomIds.isEmpty()) {
            return emptyResponse();
        }

        // Assignments and tests within this week
        List<Assignment> weekTasks = assignmentRepository
                .findWeeklyTasksByClassroomIds(classroomIds, weekStart, weekEnd);

        List<Assignment> assignments = weekTasks.stream()
                .filter(a -> TYPE_ASSIGNMENT.equals(a.getAssignmentType()))
                .collect(Collectors.toList());
        List<Assignment> tests = weekTasks.stream()
                .filter(a -> TYPE_TEST.equals(a.getAssignmentType()))
                .collect(Collectors.toList());

        // Submitted assignment IDs for this student
        List<Integer> taskIds = weekTasks.stream()
                .map(Assignment::getAssignmentID)
                .collect(Collectors.toList());

        Set<Integer> submittedIds = submissionRepository
                .findByAssignment_AssignmentIDIn(taskIds)
                .stream()
                .filter(s -> currentUser.getUserID().equals(s.getUser().getUserID()))
                .filter(s -> STATUS_SUBMITTED.equals(s.getSubmissionStatus()))
                .map(s -> s.getAssignment().getAssignmentID())
                .collect(Collectors.toSet());

        int assignmentDone = (int) assignments.stream()
                .filter(a -> submittedIds.contains(a.getAssignmentID())).count();
        int testDone = (int) tests.stream()
                .filter(t -> submittedIds.contains(t.getAssignmentID())).count();

        // Attendance within this week
        List<Attendance> allAttendances = attendanceRepository
                .findAllAttendanceByStudentUsernameAndTimeRange(
                        currentUser.getUsername(), weekStart, weekEnd);
        List<Attendance> presentAttendances = attendanceRepository
                .findAttendedByStudentUsernameAndTimeRange(
                        currentUser.getUsername(), weekStart, weekEnd);

        int attendanceTotal = allAttendances.size();
        int attendanceDone = presentAttendances.size();

        int totalTasks = assignments.size() + tests.size() + attendanceTotal;
        int completedTasks = assignmentDone + testDone + attendanceDone;
        int progressPercent = totalTasks > 0 ? (completedTasks * 100 / totalTasks) : 0;

        return ApiResponse.<WeeklyProgressResponse>builder()
                .result(WeeklyProgressResponse.builder()
                        .progressPercent(progressPercent)
                        .totalTasks(totalTasks)
                        .completedTasks(completedTasks)
                        .breakdown(WeeklyProgressResponse.Breakdown.builder()
                                .assignmentDone(assignmentDone)
                                .assignmentTotal(assignments.size())
                                .testDone(testDone)
                                .testTotal(tests.size())
                                .attendanceDone(attendanceDone)
                                .attendanceTotal(attendanceTotal)
                                .build())
                        .build())
                .build();
    }

    private ApiResponse<WeeklyProgressResponse> emptyResponse() {
        return ApiResponse.<WeeklyProgressResponse>builder()
                .result(WeeklyProgressResponse.builder()
                        .progressPercent(0)
                        .totalTasks(0)
                        .completedTasks(0)
                        .breakdown(WeeklyProgressResponse.Breakdown.builder().build())
                        .build())
                .build();
    }
}
