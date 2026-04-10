package ojt.aws.educare.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import ojt.aws.educare.entity.*;
import ojt.aws.educare.repository.*;
import ojt.aws.educare.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Fires scheduled notifications for upcoming assignments, tests and classes.
 * Runs every 5 minutes. Duplicate-prevention is handled inside
 * {@link NotificationService#createNotification} via (user + type + actionUrl) uniqueness check.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationScheduler {

    AssignmentRepository assignmentRepository;
    TimetableRepository timetableRepository;
    ClassMemberRepository classMemberRepository;
    NotificationService notificationService;

    // ── Assignment: DUE_SOON (deadline within next 24 hours) ─────────────────

    @Scheduled(fixedDelay = 300_000) // every 5 minutes
    public void notifyAssignmentDueSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24h = now.plusHours(24);

        List<Assignment> dueSoon = assignmentRepository.findPublishedByDeadlineBetween(now, in24h);
        for (Assignment assignment : dueSoon) {
            notifyClassroomStudents(
                    assignment.getClassroom(),
                    NotificationType.ASSIGNMENT_DUE_SOON,
                    "Bài tập sắp đến hạn",
                    "\"" + assignment.getTitle() + "\" sẽ hết hạn trong vòng 24 giờ tới",
                    "/student/tests/" + assignment.getAssignmentID()
            );
        }
    }

    // ── Assignment: OVERDUE (deadline passed, still PUBLISHED) ───────────────

    @Scheduled(fixedDelay = 300_000)
    public void notifyAssignmentOverdue() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusHours(1); // 1-hour look-back window

        List<Assignment> overdue = assignmentRepository.findPublishedByDeadlineBetween(since, now);
        for (Assignment assignment : overdue) {
            notifyClassroomStudents(
                    assignment.getClassroom(),
                    NotificationType.ASSIGNMENT_OVERDUE,
                    "Bài tập đã quá hạn",
                    "\"" + assignment.getTitle() + "\" đã hết hạn nộp",
                    "/student/tests/" + assignment.getAssignmentID()
            );
        }
    }

    // ── Test: TEST_UPCOMING (startTime within next 24 hours) ─────────────────

    @Scheduled(fixedDelay = 300_000)
    public void notifyTestUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        List<Assignment> upcoming = assignmentRepository
                .findPublishedTestsByStartTimeBetween(now.plusHours(23), now.plusHours(25));
        for (Assignment test : upcoming) {
            notifyClassroomStudents(
                    test.getClassroom(),
                    NotificationType.TEST_UPCOMING,
                    "Bài kiểm tra sắp diễn ra",
                    "\"" + test.getTitle() + "\" sẽ bắt đầu vào ngày mai",
                    "/student/tests/" + test.getAssignmentID()
            );
        }
    }

    // ── Test: TEST_STARTING (startTime within next 15 minutes) ───────────────

    @Scheduled(fixedDelay = 300_000)
    public void notifyTestStarting() {
        LocalDateTime now = LocalDateTime.now();
        List<Assignment> starting = assignmentRepository
                .findPublishedTestsByStartTimeBetween(now.plusMinutes(10), now.plusMinutes(20));
        for (Assignment test : starting) {
            notifyClassroomStudents(
                    test.getClassroom(),
                    NotificationType.TEST_STARTING,
                    "Bài kiểm tra sắp bắt đầu",
                    "\"" + test.getTitle() + "\" sẽ bắt đầu sau khoảng 15 phút",
                    "/student/tests/" + test.getAssignmentID()
            );
        }
    }

    // ── Class: REMINDER_CLASS (timetable startTime within 15–30 minutes) ─────

    @Scheduled(fixedDelay = 300_000)
    public void notifyClassReminder() {
        LocalDateTime now = LocalDateTime.now();
        List<Timetable> upcoming = timetableRepository
                .findByTimeRange(now.plusMinutes(15), now.plusMinutes(30));
        for (Timetable timetable : upcoming) {
            notifyClassroomStudents(
                    timetable.getClassroom(),
                    NotificationType.REMINDER_CLASS,
                    "Nhắc nhở buổi học",
                    "Buổi học" + (timetable.getTopic() != null ? " \"" + timetable.getTopic() + "\"" : "")
                            + " sẽ bắt đầu sau 15–30 phút",
                    "/student/schedule"
            );
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void notifyClassroomStudents(Classroom classroom, NotificationType type,
                                          String title, String content, String actionUrl) {
        if (classroom == null) return;
        List<ClassMember> members = classMemberRepository.findByClassroomClassID(classroom.getClassID());
        for (ClassMember member : members) {
            try {
                User studentUser = member.getStudent().getUser();
                notificationService.createNotification(studentUser, type, title, content, actionUrl);
            } catch (Exception ex) {
                log.warn("Failed to create notification for student {}: {}",
                        member.getStudent().getStudentID(), ex.getMessage());
            }
        }
    }
}
