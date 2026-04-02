package ojt.aws.educare.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.ClassroomCreateRequest;
import ojt.aws.educare.dto.request.ClassroomUpdateRequest;
import ojt.aws.educare.dto.request.ExportReportRequest;
import ojt.aws.educare.dto.response.*;
import ojt.aws.educare.dto.response.TeacherClassroomOptionResponse;
import ojt.aws.educare.service.ClassroomService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classrooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClassroomController {

    ClassroomService classroomService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PageResponse<ClassroomResponse>> getAllClassrooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return classroomService.getAllClassrooms(page, size);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ClassroomStatsResponse> getStats() {
        return classroomService.getClassroomStats();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ClassroomResponse> createClassroom(@Valid @RequestBody ClassroomCreateRequest request) {
        return classroomService.createClassroom(request);
    }

    @PutMapping("/{classId}/teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> assignTeacher(
            @PathVariable Integer classId,
            @RequestParam(required = false) Integer teacherId) {
        return classroomService.assignTeacher(classId, teacherId);
    }

    @PostMapping("/{classId}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> addStudents(
            @PathVariable Integer classId,
            @RequestBody List<Integer> studentIds) {
        return classroomService.addStudentsToClass(classId, studentIds);
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> removeStudent(
            @PathVariable Integer classId,
            @PathVariable Integer studentId) {
        return classroomService.removeStudentFromClass(classId, studentId);
    }

    @PutMapping("/{classId}/students/{studentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> toggleStudentStatus(
            @PathVariable Integer classId,
            @PathVariable Integer studentId) {
        return classroomService.toggleStudentStatusInClass(classId, studentId);
    }

    @GetMapping("/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ClassroomDetailResponse> getClassroomById(@PathVariable Integer classId) {
        return classroomService.getClassroomByID(classId);
    }

    @PutMapping("/{classId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ClassroomResponse> updateClassroom(
            @PathVariable Integer classId,
            @Valid @RequestBody ClassroomUpdateRequest request) {
        return classroomService.updateClassroom(classId, request);
    }

    @PutMapping("/{classId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> toggleClassroomStatus(@PathVariable Integer classId) {
        return classroomService.toggleClassroomStatus(classId);
    }

    @GetMapping("/teacher/me/options")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<TeacherClassroomOptionResponse>> getMyClassroomOptions() {
        return classroomService.getMyClassroomOptions();
    }

    /**
     * GET /classrooms/{classId}/students?keyword=&status=
     * Returns the filtered student roster for a class.
     *
     * @param keyword optional – search LIKE by student name or MSSV
     * @param status  optional – ONLINE | OFFLINE | ATTENTION
     */
    @GetMapping("/{classId}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<List<ClassroomDetailResponse.StudentInClassResponse>> getStudentsByClass(
            @PathVariable Integer classId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return classroomService.getStudentsByClass(classId, keyword, status);
    }

    @GetMapping("/{classId}/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ClassDashboardResponse> getClassDashboard(@PathVariable Integer classId) {
        return classroomService.getClassDashboard(classId);
    }

    @GetMapping("/{classId}/students/paged")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PageResponse<ClassStudentResponse>> getStudentsByClassPaged(
            @PathVariable Integer classId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return classroomService.getStudentsByClassPaged(classId, keyword, status, page, size);
    }

    @GetMapping("/{classId}/notifications")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<PageResponse<ClassNotificationResponse>> getClassNotifications(
            @PathVariable Integer classId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return classroomService.getClassNotifications(classId, category, page, size);
    }

    @GetMapping("/{classId}/statistics/weekly-grades")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<WeeklyGradeStatisticsResponse> getWeeklyGradeStatistics(@PathVariable Integer classId) {
        return classroomService.getWeeklyGradeStatistics(classId);
    }

    /**
     * POST /classrooms/{classId}/export
     * Accepts export options and queues/generates a class report.
     */
    @PostMapping("/{classId}/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<ExportReportResponse> exportClassReport(
            @PathVariable Integer classId,
            @Valid @RequestBody ExportReportRequest request) {
        return classroomService.exportClassReport(classId, request);
    }

    @PostMapping("/{classId}/export/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<byte[]> downloadClassReport(
            @PathVariable Integer classId,
            @Valid @RequestBody ExportReportRequest request) {
        ExportFilePayload payload = classroomService.downloadClassReport(classId, request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + payload.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, payload.getContentType())
                .body(payload.getContent());
    }
}
