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
import ojt.aws.educare.mapper.WeeklyProgressMapper;
import ojt.aws.educare.repository.*;
import ojt.aws.educare.service.WeeklyProgressService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
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
    WeeklyProgressMapper weeklyProgressMapper;

    @Override
    public ApiResponse<WeeklyProgressResponse> getMyWeeklyProgress() {
        LocalDate today = LocalDate.now();
        return getMyProgress("WEEK", today.with(DayOfWeek.MONDAY), null);
    }

    @Override
    public ApiResponse<WeeklyProgressResponse> getMyProgress(String type, LocalDate startDate, YearMonth month) {
        User currentUser = currentUserProvider.getCurrentUser();
        Student student = studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        DateRange dateRange = resolveDateRange(type, startDate, month);
        return buildProgressResponse(currentUser, student, dateRange.start(), dateRange.end());
    }

    private ApiResponse<WeeklyProgressResponse> buildProgressResponse(
            User currentUser,
            Student student,
            LocalDateTime periodStart,
            LocalDateTime periodEnd
    ) {

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
                .findTasksByClassroomIdsAndRange(classroomIds, periodStart, periodEnd);

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
                        currentUser.getUsername(), periodStart, periodEnd);
        List<Attendance> presentAttendances = attendanceRepository
                .findAttendedByStudentUsernameAndTimeRange(
                        currentUser.getUsername(), periodStart, periodEnd);

        int attendanceTotal = allAttendances.size();
        int attendanceDone = presentAttendances.size();

        int totalTasks = assignments.size() + tests.size() + attendanceTotal;
        int completedTasks = assignmentDone + testDone + attendanceDone;
        int progressPercent = totalTasks > 0 ? (completedTasks * 100 / totalTasks) : 0;

        WeeklyProgressResponse.Breakdown breakdown = weeklyProgressMapper.toBreakdown(
                assignmentDone, assignments.size(), testDone, tests.size(), attendanceDone, attendanceTotal);

        WeeklyProgressResponse response = weeklyProgressMapper.toResponse(
                progressPercent, totalTasks, completedTasks, breakdown);

        return ApiResponse.<WeeklyProgressResponse>builder().result(response).build();
    }

    private DateRange resolveDateRange(String type, LocalDate startDate, YearMonth month) {
        String normalizedType = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);

        if ("WEEK".equals(normalizedType)) {
            if (startDate == null) {
                throw new AppException(ErrorCode.TIMETABLE_DATE_INVALID);
            }
            LocalDate weekStartDate = startDate.with(DayOfWeek.MONDAY);
            LocalDate weekEndDate = weekStartDate.plusDays(6);
            return new DateRange(weekStartDate.atStartOfDay(), weekEndDate.atTime(LocalTime.MAX));
        }

        if ("MONTH".equals(normalizedType)) {
            if (month == null) {
                throw new AppException(ErrorCode.TIMETABLE_DATE_INVALID);
            }
            LocalDate monthStartDate = month.atDay(1);
            LocalDate monthEndDate = month.atEndOfMonth();
            return new DateRange(monthStartDate.atStartOfDay(), monthEndDate.atTime(LocalTime.MAX));
        }

        throw new AppException(ErrorCode.TIMETABLE_DATE_INVALID);
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
    }

    private ApiResponse<WeeklyProgressResponse> emptyResponse() {
        WeeklyProgressResponse.Breakdown breakdown = weeklyProgressMapper.toBreakdown(0, 0, 0, 0, 0, 0);
        WeeklyProgressResponse response = weeklyProgressMapper.toResponse(0, 0, 0, breakdown);
        return ApiResponse.<WeeklyProgressResponse>builder()
                .result(response)
                .build();
    }
}
