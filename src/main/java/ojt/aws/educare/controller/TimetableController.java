package ojt.aws.educare.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.*;
import ojt.aws.educare.dto.response.*;
import ojt.aws.educare.service.AttendanceService;
import ojt.aws.educare.service.TimetableService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/timetables")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TimetableController {

    TimetableService timetableService;
    AttendanceService attendanceService;

    // Lấy danh sách lịch hiển thị lên Calendar (Cần truyền ngày bắt đầu và kết thúc của tuần/tháng)
    @GetMapping
    public ApiResponse<List<TimetableResponse>> getTimetables(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return timetableService.getTimetables(start, end, includeInactive);
    }

    @GetMapping("/stats")
    public ApiResponse<TimetableStatsResponse> getStats() {
        return timetableService.getStats();
    }

    @PostMapping("/single")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TimetableResponse> createSingle(@Valid @RequestBody TimetableRequest request) {
        return timetableService.createSingleTimetable(request);
    }

    @PostMapping("/recurring")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> createRecurring(@Valid @RequestBody TimetableRecurringRequest request) {
        return timetableService.createRecurringTimetable(request);
    }

    @PutMapping("/bulk/{classId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> bulkUpdate(
            @PathVariable Integer classId,
            @Valid @RequestBody TimetableBulkUpdateRequest request) {
        return timetableService.bulkUpdateTimetable(classId, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TimetableResponse> updateSingle(
            @PathVariable Integer id,
            @Valid @RequestBody TimetableRequest request) {
        return timetableService.updateSingleTimetable(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteSingle(@PathVariable Integer id) {
        return timetableService.deleteTimetable(id);
    }

    @DeleteMapping("/class/{classId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteAllByClass(@PathVariable Integer classId) {
        return timetableService.deleteAllByClass(classId);
    }

    @PostMapping("/{timetableId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> saveAttendance(
            @PathVariable Integer timetableId,
            @RequestBody List<AttendanceRequest> requests) {

        return attendanceService.saveAttendance(timetableId, requests);
    }

    @GetMapping("/{timetableId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<AttendanceStudentResponse>> getAttendanceByTimetable(@PathVariable Integer timetableId) {
        return attendanceService.getAttendanceByTimetable(timetableId);
    }

    @GetMapping("/my-schedule")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<TimetableResponse>> getMyScheduleList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        return timetableService.getMyScheduleList(start, end, includeInactive);
    }

    @GetMapping("/my-schedule/stats")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<TeacherScheduleStatsResponse> getMyScheduleStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return timetableService.getMyScheduleStats(start, end);
    }

    @PatchMapping("/{timetableId}/meet-link")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<TimetableResponse> updateMeetLink(
            @PathVariable Integer timetableId,
            @RequestBody UpdateMeetLinkRequest request) {

        return timetableService.updateMeetLink(timetableId, request);
    }

    //lấy lịch và thống kê cho học sinh
    @GetMapping("/my-schedule/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<StudentScheduleResponse>> getStudentSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return timetableService.getStudentSchedule(start, end, includeInactive);
    }

    @GetMapping("/my-schedule/student/stats")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<StudentWeeklyStatsResponse> getStudentScheduleStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return timetableService.getStudentScheduleStats(start, end);
    }
}