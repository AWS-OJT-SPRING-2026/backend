package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.configuration.CurrentUserProvider;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.StudentDashboardResponse;
import ojt.aws.educare.entity.*;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.repository.*;
import ojt.aws.educare.service.StudentDashboardService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentDashboardServiceImpl implements StudentDashboardService {

    CurrentUserProvider currentUserProvider;
    StudentRepository studentRepository;
    ClassMemberRepository classMemberRepository;
    AssignmentRepository assignmentRepository;
    SubmissionRepository submissionRepository;
    AttendanceRepository attendanceRepository;

    // Subject → color mapping for UI
    private static final Map<String, String> SUBJECT_COLORS = Map.of(
            "Toán", "#FCE38A",
            "Tiếng Anh", "#B8B5FF",
            "Ngữ Văn", "#95E1D3",
            "Vật Lý", "#FFB5B5",
            "Hóa Học", "#FFD3B6",
            "Sinh Học", "#A8E6CF",
            "Lịch Sử", "#FFB5B5",
            "Địa Lý", "#D4A5FF",
            "Tin Học", "#85E3FF"
    );

    @Override
    public ApiResponse<StudentDashboardResponse> getMyDashboard() {
        Student student = getCurrentStudent();
        User currentUser = currentUserProvider.getCurrentUser();
        Integer userId = currentUser.getUserID();
        Integer studentId = student.getStudentID();

        // ── 1. Streak (based on attendance this week) ────────────────────────
        List<StudentDashboardResponse.StreakDayDTO> streakDays = buildStreakDays(currentUser.getUsername());
        int streakCount = calculateStreak(currentUser.getUsername());

        // ── 2. Roadmap steps (from roadmap_chapters) ─────────────────────────
        List<StudentDashboardResponse.RoadmapStepDTO> roadmapSteps = buildRoadmapSteps(student);
        int completedSteps = (int) roadmapSteps.stream().filter(StudentDashboardResponse.RoadmapStepDTO::isDone).count();
        int totalSteps = roadmapSteps.size();

        // ── 3. Deadlines (upcoming assignments for student's classrooms) ─────
        List<StudentDashboardResponse.DeadlineItemDTO> deadlines = buildDeadlines(studentId, userId);

        StudentDashboardResponse response = StudentDashboardResponse.builder()
                .streakCount(streakCount)
                .streakDays(streakDays)
                .pomodoroSessions(0)
                .totalFocusMinutes(0)
                .roadmapSteps(roadmapSteps)
                .completedRoadmapSteps(completedSteps)
                .totalRoadmapSteps(totalSteps)
                .deadlines(deadlines)
                .build();

        return ApiResponse.success("Lấy thông tin dashboard thành công", response);
    }

    // ─── Streak helpers ──────────────────────────────────────────────────────

    private List<StudentDashboardResponse.StreakDayDTO> buildStreakDays(String username) {
        LocalDate today = LocalDate.now();
        // Start from Monday of this week
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = monday.plusDays(7).atStartOfDay();

        List<Attendance> attended = attendanceRepository
                .findAttendedByStudentUsernameAndTimeRange(username, weekStart, weekEnd);

        // Which days did the student attend?
        Set<DayOfWeek> attendedDays = attended.stream()
                .map(a -> a.getTimetable().getStartTime().toLocalDate().getDayOfWeek())
                .collect(Collectors.toSet());

        String[] labels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        DayOfWeek[] days = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

        List<StudentDashboardResponse.StreakDayDTO> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            boolean done = attendedDays.contains(days[i]);
            boolean isToday = today.getDayOfWeek() == days[i];
            result.add(new StudentDashboardResponse.StreakDayDTO(labels[i], done, isToday));
        }
        return result;
    }

    private int calculateStreak(String username) {
        // Count consecutive days with attendance going backwards from yesterday
        LocalDate today = LocalDate.now();
        int streak = 0;

        // Check if attended today first
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        List<Attendance> todayAttendance = attendanceRepository
                .findAttendedByStudentUsernameAndTimeRange(username, todayStart, todayEnd);
        if (!todayAttendance.isEmpty()) {
            streak = 1;
        }

        // Go backwards day by day
        for (int i = 1; i <= 30; i++) {
            LocalDate checkDate = today.minusDays(i);
            LocalDateTime start = checkDate.atStartOfDay();
            LocalDateTime end = checkDate.plusDays(1).atStartOfDay();
            List<Attendance> dayAttendance = attendanceRepository
                    .findAttendedByStudentUsernameAndTimeRange(username, start, end);
            if (dayAttendance.isEmpty()) {
                break;
            }
            streak++;
        }
        return streak;
    }

    // ─── Roadmap helpers ─────────────────────────────────────────────────────

    private List<StudentDashboardResponse.RoadmapStepDTO> buildRoadmapSteps(Student student) {
        // For now, call the AI FastAPI /roadmap/current/{userid} internally would be complex.
        // Instead, we show a placeholder message if no roadmap data exists in the Java DB.
        // The roadmap data lives primarily in the Python AI backend's DB tables.
        // We return an empty list, and the frontend will handle the empty state gracefully.
        return Collections.emptyList();
    }

    // ─── Deadline helpers ────────────────────────────────────────────────────

    private List<StudentDashboardResponse.DeadlineItemDTO> buildDeadlines(Integer studentId, Integer userId) {
        // Find all classrooms this student belongs to
        List<ClassMember> memberships = classMemberRepository.findByStudent_StudentID(studentId);
        if (memberships.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> classroomIds = memberships.stream()
                .map(cm -> cm.getClassroom().getClassID())
                .collect(Collectors.toList());

        // Get assignments with deadline from 7 days ago to 14 days ahead
        LocalDateTime rangeStart = LocalDateTime.now().minusDays(7);
        LocalDateTime rangeEnd = LocalDateTime.now().plusDays(14);

        List<Assignment> assignments = assignmentRepository
                .findTasksByClassroomIdsAndRange(classroomIds, rangeStart, rangeEnd);

        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }

        // Check which assignments the student has already submitted
        List<Integer> assignmentIds = assignments.stream()
                .map(Assignment::getAssignmentID)
                .collect(Collectors.toList());
        List<Submission> submissions = submissionRepository.findByAssignment_AssignmentIDIn(assignmentIds);
        Set<Integer> submittedIds = submissions.stream()
                .filter(s -> s.getUser().getUserID().equals(userId))
                .map(s -> s.getAssignment().getAssignmentID())
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        List<StudentDashboardResponse.DeadlineItemDTO> deadlines = new ArrayList<>();

        for (Assignment a : assignments) {
            // Skip already submitted
            if (submittedIds.contains(a.getAssignmentID())) continue;

            String subjectName = a.getClassroom() != null && a.getClassroom().getSubject() != null
                    ? a.getClassroom().getSubject().getSubjectName()
                    : "Chung";

            // Simplify subject name for color matching (e.g. "Toán 12" → "Toán")
            String subjectKey = subjectName.split("\\s+")[0];
            String color = SUBJECT_COLORS.getOrDefault(subjectKey, "#B8B5FF");

            // Determine deadline text and urgency
            LocalDateTime deadline = a.getAssignmentType() != null && a.getAssignmentType().equals("TEST")
                    ? a.getStartTime()
                    : a.getDeadline();

            if (deadline == null) continue;

            boolean overdue = deadline.isBefore(now);
            boolean urgent = !overdue && ChronoUnit.HOURS.between(now, deadline) <= 24;
            String dueText = formatDeadlineText(deadline, now, overdue);

            String action = a.getAssignmentType() != null && a.getAssignmentType().equals("TEST")
                    ? "schedule"
                    : "exercises";

            deadlines.add(new StudentDashboardResponse.DeadlineItemDTO(
                    a.getAssignmentID().longValue(),
                    subjectName,
                    color,
                    a.getTitle(),
                    dueText,
                    urgent,
                    action,
                    overdue
            ));
        }

        // Sort: urgent first, then by deadline ascending
        deadlines.sort((a, b) -> {
            if (a.isUrgent() != b.isUrgent()) return a.isUrgent() ? -1 : 1;
            if (a.isMissing() != b.isMissing()) return a.isMissing() ? 1 : -1;
            return 0;
        });

        return deadlines;
    }

    private String formatDeadlineText(LocalDateTime deadline, LocalDateTime now, boolean overdue) {
        if (overdue) return "Đã quá hạn";

        long hours = ChronoUnit.HOURS.between(now, deadline);
        if (hours < 1) return "Sắp hết hạn";
        if (hours < 24) return hours + " giờ nữa";

        long days = ChronoUnit.DAYS.between(now.toLocalDate(), deadline.toLocalDate());
        if (days == 1) return "Ngày mai";
        if (days <= 7) return days + " ngày nữa";

        DayOfWeek dow = deadline.getDayOfWeek();
        return "Tuần sau";
    }

    // ─── Common ──────────────────────────────────────────────────────────────

    private Student getCurrentStudent() {
        User currentUser = currentUserProvider.getCurrentUser();
        return studentRepository.findByUser_UserID(currentUser.getUserID())
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
    }
}
