package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.configuration.CurrentUserProvider;
import ojt.aws.educare.dto.request.ClassroomCreateRequest;
import ojt.aws.educare.dto.request.ClassroomUpdateRequest;
import ojt.aws.educare.dto.request.ExportReportRequest;
import ojt.aws.educare.dto.response.*;
import ojt.aws.educare.dto.response.TeacherClassroomOptionResponse;
import ojt.aws.educare.entity.*;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.ClassMemberMapper;
import ojt.aws.educare.mapper.ClassroomMapper;
import ojt.aws.educare.mapper.PageResponseMapper;
import ojt.aws.educare.mapper.WeeklyGradeStatsMapper;
import ojt.aws.educare.repository.*;
import ojt.aws.educare.repository.projection.WeeklyGradeAggregationProjection;
import ojt.aws.educare.service.ClassroomService;
import ojt.aws.educare.service.NotificationService;
import ojt.aws.educare.service.S3UploadService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.awt.Color;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClassroomServiceImpl implements  ClassroomService {
    ClassroomRepository classroomRepository;
    ClassMemberRepository classMemberRepository;
    SubjectRepository subjectRepository;
    TeacherRepository teacherRepository;
    StudentRepository studentRepository;
    UserRepository userRepository;
    AssignmentRepository assignmentRepository;
    SubmissionRepository submissionRepository;
    AttendanceRepository attendanceRepository;
    TimetableRepository timetableRepository;
    S3UploadService s3UploadService;
    CurrentUserProvider currentUserProvider;
    NotificationService notificationService;

    ClassroomMapper classroomMapper;
    ClassMemberMapper classMemberMapper;
    WeeklyGradeStatsMapper weeklyGradeStatsMapper;
    PageResponseMapper pageResponseMapper;

    @Override
    public ApiResponse<PageResponse<ClassroomResponse>> getAllClassrooms(int page, int size) {
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(
                        Sort.Order.desc("updatedAt"),
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("classID")
                ));
        User currentUser = currentUserProvider.getCurrentUser();

        String roleName = currentUser.getRole() != null && currentUser.getRole().getRoleName() != null
                ? currentUser.getRole().getRoleName().toUpperCase(Locale.ROOT)
                : "";

        Page<Classroom> classroomPage;
        if ("TEACHER".equals(roleName)) {
            Teacher teacher = teacherRepository.findByUser(currentUser)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_IS_NOT_TEACHER));
            classroomPage = classroomRepository.findByTeacher_TeacherID(teacher.getTeacherID(), pageable);
        } else {
            classroomPage = classroomRepository.findAll(pageable);
        }

        List<ClassroomResponse> responses = classroomMapper.toClassroomResponseList(classroomPage.getContent());

        PageResponse<ClassroomResponse> pageResponse = pageResponseMapper.toPageResponse(
                page, size, classroomPage.getTotalPages(), classroomPage.getTotalElements(), responses);

        return ApiResponse.success("Lấy danh sách lớp học thành công", pageResponse);
    }

    @Override
    public ApiResponse<ClassroomStatsResponse> getClassroomStats() {
        long totalClasses = classroomRepository.count();
        long activeClasses = classroomRepository.countByStatus("ACTIVE");
        long unassignedClasses = classroomRepository.countByTeacherIsNull();
        long totalEnrolledStudents = classMemberRepository.count();
        int avgClassSize = totalClasses == 0 ? 0 : (int) Math.round((double) totalEnrolledStudents / totalClasses);

        ClassroomStatsResponse stats = classroomMapper.toClassroomStatsResponse(
                totalClasses, activeClasses, unassignedClasses, avgClassSize);
        
        return ApiResponse.success("Lấy thống kê thành công", stats);
    }

    @Override
    @Transactional
    public ApiResponse<ClassroomResponse> createClassroom(ClassroomCreateRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectID())
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        Teacher teacher = null;
        if (request.getTeacherId() != null) {
            teacher = teacherRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));
        }

        Classroom classroom = classroomMapper.toClassroom(request);
        classroom.setSubject(subject);
        classroom.setTeacher(teacher);

        Classroom savedClassroom = classroomRepository.save(classroom);
        return ApiResponse.success("Tạo lớp học thành công", classroomMapper.toClassroomResponse(savedClassroom));
    }

    @Override
    @Transactional
    public ApiResponse<Void> assignTeacher(Integer classID, Integer teacherID) {
        Classroom classroom = classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        Teacher oldTeacher = classroom.getTeacher();

        if (teacherID == null) {
            classroom.setTeacher(null);
            classroomRepository.save(classroom);
            syncTeacherForClassTimetables(classID, null);
            return ApiResponse.success("Đã gỡ phân công giáo viên khỏi lớp học", null);
        }

        Teacher newTeacher = teacherRepository.findById(teacherID)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));

        Teacher currentTeacher = classroom.getTeacher();

        if (currentTeacher != null && currentTeacher.getTeacherID().equals(teacherID)) {
            return ApiResponse.success("Giáo viên này đang phụ trách lớp học này rồi!", null);
        }

        String message = (currentTeacher == null)
                ? "Đã phân công giáo viên thành công"
                : "Đã đổi giáo viên thành công";

        classroom.setTeacher(newTeacher);
        classroomRepository.save(classroom);

        int affectedSessions = syncTeacherForClassTimetables(classID, newTeacher);

        notificationService.notifyClassroomStudents(
                classID,
                NotificationType.TEACHER_CHANGED,
                "Thay đổi giáo viên",
                buildTeacherChangedContent(oldTeacher, newTeacher, affectedSessions),
                "/student/schedule"
        );

        return ApiResponse.success(message, null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> addStudentsToClass(Integer classID, List<Integer> studentIDs) {
        Classroom classroom = classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        // 2. Lấy danh sách ID học sinh HIỆN TẠI đang có trong lớp từ DB
        List<Integer> currentStudentIds = classMemberRepository.findByClassroomClassID(classID).stream()
                .map(cm -> cm.getStudent().getStudentID())
                .toList();

        // 3. Phân loại: Tìm ra những học sinh CẦN THÊM
        List<Integer> idsToAdd = studentIDs.stream()
                .filter(id -> !currentStudentIds.contains(id))
                .toList();

        // 4. Phân loại: Tìm ra những học sinh CẦN XÓA
        List<Integer> idsToRemove = currentStudentIds.stream()
                .filter(id -> !studentIDs.contains(id))
                .toList();

        // 5. Kiểm tra sĩ số sau khi tính toán
        int finalStudentCount = currentStudentIds.size() - idsToRemove.size() + idsToAdd.size();
        if (finalStudentCount > classroom.getMaxStudents()) {
            throw new AppException(ErrorCode.CLASSROOM_FULL);
        }

        // 6. Thực hiện XÓA bằng JPQL query trực tiếp (tránh conflict với Hibernate cache/orphanRemoval)
        if (!idsToRemove.isEmpty()) {
            classMemberRepository.deleteByClassIDAndStudentIDIn(classID, idsToRemove);
        }

        // 7. Flush để đảm bảo delete đã được commit trước khi insert
        classMemberRepository.flush();

        // 8. Thực hiện THÊM MỚI
        for (Integer studentId : idsToAdd) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

            ClassMemberID memberID = new ClassMemberID(classID, studentId);
            ClassMember newMember = classMemberMapper.toClassMember(memberID, classroom, student);

            classMemberRepository.save(newMember);
        }

        return ApiResponse.success("Cập nhật danh sách học sinh thành công", null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> removeStudentFromClass(Integer classID, Integer studentID) {
        ClassMemberID memberID = new ClassMemberID(classID, studentID);

        ClassMember classMember = classMemberRepository.findById(memberID)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_IN_CLASS));

        classMemberRepository.delete(classMember);

        return ApiResponse.success("Đã xóa học sinh khỏi lớp học", null);
    }

    //Đình chỉ
    @Override
    @Transactional
    public ApiResponse<Void> toggleStudentStatusInClass(Integer classID, Integer studentID) {
        ClassMemberID memberID = new ClassMemberID(classID, studentID);

        ClassMember classMember = classMemberRepository.findById(memberID)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        boolean isActive = "ACTIVE".equalsIgnoreCase(classMember.getStatus());
        classMember.setStatus(isActive ? "INACTIVE" : "ACTIVE");

        classMemberRepository.save(classMember);

        String message = isActive ? "Đã đình chỉ học sinh trong lớp này" : "Đã mở lại trạng thái học tập cho học sinh";
        return ApiResponse.success(message, null);
    }

    @Override
    @Transactional
    public ApiResponse<ClassroomDetailResponse> getClassroomByID(Integer classID) {
        Classroom classroom = classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        ClassroomDetailResponse response = classroomMapper.toClassroomDetailResponse(classroom);
        if (response.getStudents() != null) {
            response.getStudents().forEach(student -> {
                if (student.getAvatarUrl() != null && !student.getAvatarUrl().isBlank()) {
                    student.setAvatarUrl(s3UploadService.resolveFileUrl(student.getAvatarUrl()));
                }
            });
        }
        return ApiResponse.success("Lấy thông tin lớp học thành công", response);
    }

    @Override
    @Transactional
    public ApiResponse<ClassroomResponse> updateClassroom(Integer classID, ClassroomUpdateRequest request) {
        Classroom classroom = classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        Teacher oldTeacher = classroom.getTeacher();
        boolean teacherChanged = false;

        if (request.getSubjectID() != null && (classroom.getSubject() == null || !classroom.getSubject().getSubjectID().equals(request.getSubjectID()))) {
            Subject subject = subjectRepository.findById(request.getSubjectID())
                    .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
            classroom.setSubject(subject);
        }

        if (request.getTeacherID() == null) {
            teacherChanged = classroom.getTeacher() != null;
            classroom.setTeacher(null);
        } else if (classroom.getTeacher() == null || !classroom.getTeacher().getTeacherID().equals(request.getTeacherID())) {
            Teacher teacher = teacherRepository.findById(request.getTeacherID())
                    .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));
            classroom.setTeacher(teacher);
            teacherChanged = true;
        }

        classroomMapper.updateClassroomFromRequest(classroom, request);

        Classroom savedClassroom = classroomRepository.save(classroom);

        if (teacherChanged) {
            int affectedSessions = syncTeacherForClassTimetables(classID, savedClassroom.getTeacher());
            if (savedClassroom.getTeacher() != null) {
                notificationService.notifyClassroomStudents(
                        classID,
                        NotificationType.TEACHER_CHANGED,
                        "Thay đổi giáo viên",
                        buildTeacherChangedContent(oldTeacher, savedClassroom.getTeacher(), affectedSessions),
                        "/student/schedule"
                );
            }
        }

        return ApiResponse.success("Cập nhật lớp học thành công", classroomMapper.toClassroomResponse(savedClassroom));
    }

    @Override
    @Transactional
    public ApiResponse<Void> toggleClassroomStatus(Integer classID) {
        Classroom classroom = classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        boolean isActive = "ACTIVE".equalsIgnoreCase(classroom.getStatus());
        classroom.setStatus(isActive ? "INACTIVE" : "ACTIVE");
        classroomRepository.save(classroom);

        String message = isActive ? "Đã khóa (tạm dừng) lớp học thành công" : "Đã mở lại lớp học thành công";
        return ApiResponse.success(message, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TeacherClassroomOptionResponse>> getMyClassroomOptions() {
        User currentUser = currentUserProvider.getCurrentUser();

        Teacher teacher = teacherRepository.findByUser(currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.USER_IS_NOT_TEACHER));

        List<Classroom> classrooms = classroomRepository.findByTeacher_TeacherIDOrderByClassNameAsc(teacher.getTeacherID());
        List<TeacherClassroomOptionResponse> options = classroomMapper.toTeacherClassroomOptionResponseList(classrooms);

        return ApiResponse.success("Lấy danh sách lớp học của giáo viên thành công", options);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ClassDashboardResponse> getClassDashboard(Integer classID) {
        List<ClassStudentResponse> students = buildStudentRows(classID);

        int total = students.size();
        int online = (int) students.stream().filter(s -> "ONLINE".equals(s.getStatus())).count();
        int offline = (int) students.stream().filter(s -> "OFFLINE".equals(s.getStatus())).count();
        int attention = (int) students.stream().filter(s -> "ATTENTION".equals(s.getStatus())).count();
        double averageGpa = students.stream()
                .map(ClassStudentResponse::getGpa)
                .filter(gpa -> gpa != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        String className = classroomRepository.findById(classID)
                .map(Classroom::getClassName)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        ClassDashboardResponse response = classroomMapper.toClassDashboardResponse(
                classID, className, total, online, offline, attention, round2(averageGpa));

        return ApiResponse.success("Lấy dữ liệu tổng quan lớp học thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<ClassStudentResponse>> getStudentsByClassPaged(
            Integer classID,
            String keyword,
            String status,
            int page,
            int size) {

        validateStatusFilter(status);

        List<ClassStudentResponse> students = buildStudentRows(classID);

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase(Locale.ROOT);
            students = students.stream()
                    .filter(s -> (s.getFullName() != null && s.getFullName().toLowerCase(Locale.ROOT).contains(kw))
                            || (s.getMssv() != null && s.getMssv().contains(kw)))
                    .toList();
        }

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            String filter = status.toUpperCase(Locale.ROOT);
            students = students.stream().filter(s -> filter.equalsIgnoreCase(s.getStatus())).toList();
        }

        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int fromIndex = Math.min((safePage - 1) * safeSize, students.size());
        int toIndex = Math.min(fromIndex + safeSize, students.size());
        List<ClassStudentResponse> pageData = students.subList(fromIndex, toIndex);

        PageResponse<ClassStudentResponse> response = pageResponseMapper.toPageResponse(
                safePage,
                safeSize,
                (int) Math.ceil(students.size() / (double) safeSize),
                students.size(),
                pageData);

        return ApiResponse.success("Lấy danh sách học sinh thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<ClassNotificationResponse>> getClassNotifications(
            Integer classID,
            String category,
            int page,
            int size) {

        classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        String normalizedCategory = (category == null || category.isBlank())
                ? "ALL"
                : category.toUpperCase(Locale.ROOT);

        if (!VALID_NOTIFICATION_CATEGORIES.contains(normalizedCategory)) {
            throw new AppException(ErrorCode.STATUS_REQUIRED);
        }

        List<ClassStudentResponse> students = buildStudentRows(classID);
        List<ClassNotificationResponse> notifications = new ArrayList<>();

        long idSeed = 1L;

        for (ClassStudentResponse s : students.stream().filter(st -> "ATTENTION".equals(st.getStatus())).toList()) {
            notifications.add(classroomMapper.toClassNotificationResponse(
                    idSeed++,
                    "GRADE",
                    "Cảnh báo học tập",
                    String.format("%s có GPA %.1f và tiến độ %.0f%%", s.getFullName(), safeDouble(s.getGpa()), safeDouble(s.getCompletionRate())),
                    LocalDateTime.now()));
        }

        LocalDateTime from = LocalDateTime.now().minusDays(7);
        List<Attendance> recentAttendances = attendanceRepository.findByClassIdAndUpdatedAtBetween(classID, from, LocalDateTime.now());
        Map<String, Long> absentByStudent = recentAttendances.stream()
                .filter(a -> "ABSENT".equalsIgnoreCase(a.getStatus()) || "LATE".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.groupingBy(a -> a.getStudent().getFullName(), Collectors.counting()));

        for (Map.Entry<String, Long> item : absentByStudent.entrySet()) {
            notifications.add(classroomMapper.toClassNotificationResponse(
                    idSeed++,
                    "ATTENDANCE",
                    "Điểm danh cần chú ý",
                    String.format("%s có %d buổi vắng/trễ trong 7 ngày gần đây", item.getKey(), item.getValue()),
                    LocalDateTime.now().minusHours(2)));
        }

        notifications.add(classroomMapper.toClassNotificationResponse(
                idSeed,
                "SYSTEM",
                "Nhắc nhở hệ thống",
                "Bạn có thể dùng chức năng Xuất báo cáo để tải dữ liệu lớp theo bộ lọc hiện tại.",
                LocalDateTime.now().minusHours(6)));

        if (!"ALL".equals(normalizedCategory)) {
            notifications = notifications.stream()
                    .filter(n -> normalizedCategory.equalsIgnoreCase(n.getCategory()))
                    .toList();
        }

        notifications = notifications.stream()
                .sorted(Comparator.comparing(ClassNotificationResponse::getCreatedAt).reversed())
                .toList();

        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int fromIndex = Math.min((safePage - 1) * safeSize, notifications.size());
        int toIndex = Math.min(fromIndex + safeSize, notifications.size());

        PageResponse<ClassNotificationResponse> response = pageResponseMapper.toPageResponse(
                safePage,
                safeSize,
                (int) Math.ceil(notifications.size() / (double) safeSize),
                notifications.size(),
                notifications.subList(fromIndex, toIndex));

        return ApiResponse.success("Lấy timeline thông báo thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<WeeklyGradeStatisticsResponse> getWeeklyGradeStatistics(Integer classID) {
        Classroom classroom = classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        User currentUser = getCurrentUser();
        ensureTeacherCanAccessClassroom(classroom, currentUser);

        LocalDate weekStartDate = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEndExclusive = weekStart.plusDays(6);

        List<WeeklyGradeAggregationProjection> rows =
                submissionRepository.aggregateWeeklyGrades(classID, weekStart, weekEndExclusive);

        Map<Integer, WeeklyGradeDayResponse> byDay = new HashMap<>();
        for (WeeklyGradeAggregationProjection row : rows) {
            WeeklyGradeDayResponse mapped = weeklyGradeStatsMapper.toWeeklyGradeDayResponse(row);
            mapped.setDayLabel(mapDayLabel(mapped.getDayOfWeek()));
            byDay.put(mapped.getDayOfWeek(), mapped);
        }

        List<WeeklyGradeDayResponse> days = new ArrayList<>();
        for (int day = 1; day <= 6; day++) {
            WeeklyGradeDayResponse dayStat = byDay.get(day);
            if (dayStat == null) {
                dayStat = weeklyGradeStatsMapper.toDefaultWeeklyGradeDayResponse(day, mapDayLabel(day));
            }
            days.add(dayStat);
        }

        WeeklyGradeStatisticsResponse response = classroomMapper.toWeeklyGradeStatisticsResponse(
                classID,
                classroom.getMaxStudents() == null ? 0 : classroom.getMaxStudents(),
                days);

        return ApiResponse.success("Lấy thống kê điểm theo tuần thành công", response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Student roster filtering
    // ─────────────────────────────────────────────────────────────────────────

    private static final List<String> VALID_FILTER_STATUSES =
            List.of("ALL", "ONLINE", "OFFLINE", "ATTENTION");

    private static final int ONLINE_WINDOW_MINUTES = 15;
    private static final int MISSING_ATTENTION_THRESHOLD = 2;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ClassroomDetailResponse.StudentInClassResponse>> getStudentsByClass(
            Integer classID, String keyword, String status) {

        Classroom classroom = classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        if (status != null && !status.isBlank()
                && !VALID_FILTER_STATUSES.contains(status.toUpperCase())) {
            throw new AppException(ErrorCode.STATUS_REQUIRED);
        }

        // Reuse existing mapper — detail response already contains the full student list.
        ClassroomDetailResponse detail = classroomMapper.toClassroomDetailResponse(classroom);
        List<ClassroomDetailResponse.StudentInClassResponse> students = detail.getStudents();

        students.forEach(student -> {
            if (student.getAvatarUrl() != null && !student.getAvatarUrl().isBlank()) {
                student.setAvatarUrl(s3UploadService.resolveFileUrl(student.getAvatarUrl()));
            }
        });

        // Filter by keyword (name or MSSV)
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase().trim();
            students = students.stream()
                    .filter(s -> s.getFullName().toLowerCase().contains(kw)
                            || String.valueOf(s.getStudentID()).contains(kw))
                    .toList();
        }

        // Filter by status
        // "ONLINE" / "OFFLINE" map to memberStatus ACTIVE / INACTIVE.
        // "ATTENTION" (Cần chú ý) maps to INACTIVE as a proxy until
        //  score/completion fields are persisted.
        if (status != null && !status.isBlank()) {
            String upper = status.toUpperCase();
            students = switch (upper) {
                case "ONLINE"     -> students.stream()
                        .filter(s -> "ACTIVE".equalsIgnoreCase(s.getMemberStatus()))
                        .toList();
                case "OFFLINE"    -> students.stream()
                        .filter(s -> "INACTIVE".equalsIgnoreCase(s.getMemberStatus()))
                        .toList();
                case "ATTENTION"  ->
                    // TODO: Replace with score/completion-based logic when those
                    //        fields are available in the ClassMember entity.
                    students.stream()
                        .filter(s -> "INACTIVE".equalsIgnoreCase(s.getMemberStatus()))
                        .toList();
                default           -> students;
            };
        }

        return ApiResponse.success("Lấy danh sách học sinh thành công", students);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Report export
    // ─────────────────────────────────────────────────────────────────────────

    private static final List<String> VALID_FORMATS =
            List.of("EXCEL", "PDF", "CSV");
    private static final List<String> VALID_DATA_TYPES =
            List.of("GRADEBOOK", "GRADES", "ATTENDANCE", "PROGRESS");
    private static final List<String> VALID_TIME_RANGES =
            List.of("THIS_WEEK", "THIS_MONTH", "CURRENT_SEMESTER");
    private static final List<String> VALID_NOTIFICATION_CATEGORIES =
            List.of("ALL", "GRADE", "ATTENDANCE", "SYSTEM");

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ExportReportResponse> exportClassReport(
            Integer classID, ExportReportRequest request) {
        ExportFilePayload payload = downloadClassReport(classID, request);

        ExportReportResponse response = classroomMapper.toExportReportResponse(payload, request);

        return ApiResponse.success("Yêu cầu xuất báo cáo thành công", response);
    }

    @Override
    @Transactional(readOnly = true)
    public ExportFilePayload downloadClassReport(Integer classID, ExportReportRequest request) {
        Classroom classroom = classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        validateExportRequest(request);

        String format = request.getFormat().toUpperCase(Locale.ROOT);
        String extension = switch (format) {
            case "PDF" -> "pdf";
            case "CSV" -> "csv";
            default -> "xlsx";
        };

        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = String.format("Bao_cao_Lop_%d_%s.%s", classID, datePart, extension);

        try {
            byte[] content;
            String contentType;
            LocalDateTime[] range = resolveTimeRange(request.getTimeRange());
            List<ClassStudentResponse> exportRows = buildStudentRows(classID, range[0], range[1]);

            switch (format) {
                case "CSV" -> {
                    content = generateCsv(exportRows);
                    contentType = "text/csv";
                }
                case "PDF" -> {
                    content = generatePdf(classroom, request, exportRows);
                    contentType = MediaType.APPLICATION_PDF_VALUE;
                }
                default -> {
                    content = generateExcel(classroom, request, exportRows);
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                }
            }

            return classroomMapper.toExportFilePayload(fileName, contentType, content);
        } catch (IOException e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private void validateStatusFilter(String status) {
        if (status != null && !status.isBlank()
                && !VALID_FILTER_STATUSES.contains(status.toUpperCase(Locale.ROOT))) {
            throw new AppException(ErrorCode.STATUS_REQUIRED);
        }
    }

    private void validateExportRequest(ExportReportRequest request) {
        if (!VALID_FORMATS.contains(request.getFormat().toUpperCase(Locale.ROOT))) {
            throw new AppException(ErrorCode.EXPORT_FORMAT_INVALID);
        }

        if (request.getDataTypes() == null || request.getDataTypes().isEmpty()) {
            throw new AppException(ErrorCode.EXPORT_DATA_TYPES_REQUIRED);
        }

        for (String dt : request.getDataTypes()) {
            if (!VALID_DATA_TYPES.contains(dt.toUpperCase(Locale.ROOT))) {
                throw new AppException(ErrorCode.EXPORT_DATA_TYPE_INVALID);
            }
        }

        if (!VALID_TIME_RANGES.contains(request.getTimeRange().toUpperCase(Locale.ROOT))) {
            throw new AppException(ErrorCode.EXPORT_TIME_RANGE_INVALID);
        }
    }

    private List<ClassStudentResponse> buildStudentRows(Integer classID) {
        return buildStudentRows(classID, null, null);
    }

    private List<ClassStudentResponse> buildStudentRows(Integer classID, LocalDateTime start, LocalDateTime end) {
        classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        List<ClassMember> members = classMemberRepository.findByClassroomClassIDOrderByStudentFullNameAsc(classID);
        List<Assignment> assignments = assignmentRepository.findByClassroom_ClassIDOrderByCreatedAtDesc(classID);
        List<Integer> assignmentIds = assignments.stream().map(Assignment::getAssignmentID).toList();

        List<Submission> submissions = assignmentIds.isEmpty()
                ? List.of()
                : submissionRepository.findByAssignment_AssignmentIDIn(assignmentIds);

        if (start != null && end != null) {
            submissions = submissions.stream()
                    .filter(s -> s.getSubmittedAt() != null
                            && !s.getSubmittedAt().isBefore(start)
                            && !s.getSubmittedAt().isAfter(end))
                    .toList();
        }

        Map<Integer, List<Submission>> submissionsByUserId = submissions.stream()
                .filter(s -> s.getUser() != null && s.getUser().getUserID() != null)
                .collect(Collectors.groupingBy(s -> s.getUser().getUserID()));

        int totalAssignments = assignments.size();
        return members.stream().map(member -> {
            Student student = member.getStudent();
            User user = student.getUser();
            List<Submission> studentSubmissions = submissionsByUserId.getOrDefault(user.getUserID(), List.of());

            Set<Integer> submittedAssignmentIds = studentSubmissions.stream()
                    .filter(s -> s.getSubmittedAt() != null)
                    .filter(s -> s.getSubmissionStatus() == null || !"MISSING".equalsIgnoreCase(s.getSubmissionStatus()))
                    .map(s -> s.getAssignment().getAssignmentID())
                    .collect(Collectors.toSet());

            int missingCountFromSubmission = (int) studentSubmissions.stream()
                    .filter(s -> "MISSING".equalsIgnoreCase(s.getSubmissionStatus()))
                    .count();

            int inferredMissingCount = (int) assignments.stream()
                    .filter(a -> {
                        LocalDateTime due = resolveAssignmentDueTime(a);
                        if (due == null || LocalDateTime.now().isBefore(due)) {
                            return false;
                        }
                        return studentSubmissions.stream()
                                .noneMatch(s -> s.getAssignment() != null
                                        && Objects.equals(s.getAssignment().getAssignmentID(), a.getAssignmentID()));
                    })
                    .count();

            int missingCount = missingCountFromSubmission + inferredMissingCount;

            double completion = totalAssignments == 0
                    ? 0.0
                    : (submittedAssignmentIds.size() * 100.0) / totalAssignments;

            double gpaFromSubmissions = studentSubmissions.stream()
                    .filter(s -> s.getScore() != null)
                    .map(Submission::getScore)
                    .mapToDouble(score -> score.doubleValue())
                    .average()
                    .orElse(0.0);

            int gradedCount = (int) studentSubmissions.stream().filter(s -> s.getScore() != null).count() + inferredMissingCount;
            double totalScore = studentSubmissions.stream()
                    .filter(s -> s.getScore() != null)
                    .map(Submission::getScore)
                    .mapToDouble(score -> score.doubleValue())
                    .sum();
            double gpa = gradedCount > 0 ? totalScore / gradedCount : gpaFromSubmissions;

            String status = resolveStudentStatus(user.getLastActiveAt(), completion, gpa, missingCount);

            return classroomMapper.toClassStudentResponse(
                    student.getStudentID(),
                    student.getFullName(),
                    String.valueOf(student.getStudentID()),
                    s3UploadService.resolveFileUrl(user.getAvatarUrl()),
                    round2(completion),
                    round2(gpa),
                    missingCount,
                    user.getLastActiveAt(),
                    status);
        }).toList();
    }

    private String resolveStudentStatus(LocalDateTime lastActiveTime, double completionRate, double gpa, int missingCount) {
        if (lastActiveTime != null && lastActiveTime.isAfter(LocalDateTime.now().minusMinutes(ONLINE_WINDOW_MINUTES))) {
            return "ONLINE";
        }

        if (completionRate < 50 || gpa < 5 || missingCount >= MISSING_ATTENTION_THRESHOLD) {
            return "ATTENTION";
        }

        return "OFFLINE";
    }

    private LocalDateTime resolveAssignmentDueTime(Assignment assignment) {
        if (assignment == null) {
            return null;
        }
        if ("TEST".equalsIgnoreCase(assignment.getAssignmentType())) {
            return assignment.getEndTime();
        }
        return assignment.getDeadline();
    }

    private String mapDayLabel(Integer dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "T2";
            case 2 -> "T3";
            case 3 -> "T4";
            case 4 -> "T5";
            case 5 -> "T6";
            case 6 -> "T7";
            default -> "CN";
        };
    }

    private User getCurrentUser() {
        return currentUserProvider.getCurrentUser();
    }

    private void ensureTeacherCanAccessClassroom(Classroom classroom, User currentUser) {
        String roleName = currentUser.getRole() != null && currentUser.getRole().getRoleName() != null
                ? currentUser.getRole().getRoleName().toUpperCase(Locale.ROOT)
                : "";

        if (!"TEACHER".equals(roleName)) {
            return;
        }

        Teacher teacher = teacherRepository.findByUser(currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.USER_IS_NOT_TEACHER));

        Integer ownerTeacherId = classroom.getTeacher() == null ? null : classroom.getTeacher().getTeacherID();
        if (ownerTeacherId == null || !ownerTeacherId.equals(teacher.getTeacherID())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private byte[] generateCsv(List<ClassStudentResponse> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF"); // UTF-8 BOM for Excel Vietnamese support
        sb.append("ID,Ho ten,Hoan thanh,GPA,Bai bo lo,Trang thai\n");
        for (ClassStudentResponse row : rows) {
            sb.append(formatMssv(row.getMssv())).append(',')
                    .append('"').append(row.getFullName().replace("\"", "''")).append('"').append(',')
                    .append(String.format(Locale.ROOT, "%.2f%%", safeDouble(row.getCompletionRate()))).append(',')
                    .append(String.format(Locale.ROOT, "%.2f", safeDouble(row.getGpa()))).append(',')
                    .append('"').append(mapMissingLabel(row.getMissingCount())).append('"').append(',')
                    .append(mapStatusVi(row.getStatus()))
                    .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generateExcel(Classroom classroom, ExportReportRequest request, List<ClassStudentResponse> rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Bao cao lop");

            Row title = sheet.createRow(0);
            Cell titleCell = title.createCell(0);
            titleCell.setCellValue("BÁO CÁO CHI TIẾT LỚP " + classroom.getClassName());

            Row meta = sheet.createRow(1);
            meta.createCell(0).setCellValue("Loại dữ liệu: " + mapDataTypesVi(request.getDataTypes()));
            meta.createCell(1).setCellValue("Khoảng thời gian: " + mapTimeRangeVi(request.getTimeRange()));

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(3);
            String[] columns = {"ID", "Họ tên", "Hoàn thành", "GPA", "Bài bỏ lỡ", "Trạng thái"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 4;
            for (ClassStudentResponse row : rows) {
                Row excelRow = sheet.createRow(rowIdx++);
                excelRow.createCell(0).setCellValue(formatMssv(row.getMssv()));
                excelRow.createCell(1).setCellValue(row.getFullName());
                excelRow.createCell(2).setCellValue(String.format(Locale.ROOT, "%.2f%%", safeDouble(row.getCompletionRate())));
                excelRow.createCell(3).setCellValue(String.format(Locale.ROOT, "%.2f", safeDouble(row.getGpa())));
                excelRow.createCell(4).setCellValue(mapMissingLabel(row.getMissingCount()));
                excelRow.createCell(5).setCellValue(mapStatusVi(row.getStatus()));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private byte[] generatePdf(Classroom classroom, ExportReportRequest request, List<ClassStudentResponse> rows) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            ReportPdfPageEvent pageEvent = new ReportPdfPageEvent();
            writer.setPageEvent(pageEvent);
            document.open();

            BaseFont baseFont = createUnicodeBaseFont();
            Font titleFont = new Font(baseFont, 18, Font.BOLD, Color.BLACK);
            Font metaLabelFont = new Font(baseFont, 11, Font.BOLD, Color.BLACK);
            Font metaValueFont = new Font(baseFont, 11, Font.NORMAL, Color.BLACK);
            Font tableHeaderFont = new Font(baseFont, 10, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(baseFont, 10, Font.NORMAL, Color.BLACK);

            Paragraph title = new Paragraph("BÁO CÁO CHI TIẾT LỚP " + classroom.getClassName(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10f);
            document.add(title);

            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{1.4f, 3.6f});
            addMetaRow(metaTable, "Loại dữ liệu", mapDataTypesVi(request.getDataTypes()), metaLabelFont, metaValueFont);
            addMetaRow(metaTable, "Khoảng thời gian", mapTimeRangeVi(request.getTimeRange()), metaLabelFont, metaValueFont);
            addMetaRow(metaTable, "Định dạng", mapFormatVi(request.getFormat()), metaLabelFont, metaValueFont);
            metaTable.setSpacingAfter(10f);
            document.add(metaTable);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 2.4f, 1.3f, 0.9f, 1.4f, 1.2f});

            addTableHeaderCell(table, "MSSV", tableHeaderFont);
            addTableHeaderCell(table, "Họ tên", tableHeaderFont);
            addTableHeaderCell(table, "Hoàn thành", tableHeaderFont);
            addTableHeaderCell(table, "GPA", tableHeaderFont);
            addTableHeaderCell(table, "Bài bỏ lỡ", tableHeaderFont);
            addTableHeaderCell(table, "Trạng thái", tableHeaderFont);

            boolean zebra = false;
            for (ClassStudentResponse row : rows) {
                Color rowBg = zebra ? new Color(245, 245, 245) : Color.WHITE;
                zebra = !zebra;

                addBodyCell(table, formatMssv(row.getMssv()), bodyFont, rowBg, null);
                addBodyCell(table, row.getFullName(), bodyFont, rowBg, null);
                addBodyCell(table, String.format("%.2f%%", safeDouble(row.getCompletionRate())), bodyFont, rowBg, null);
                addBodyCell(table, String.format("%.2f", safeDouble(row.getGpa())), bodyFont, rowBg, null);
                addBodyCell(table, mapMissingLabel(row.getMissingCount()), bodyFont, rowBg, null);
                addBodyCell(table, mapStatusVi(row.getStatus()), bodyFont, rowBg, mapStatusColor(row.getStatus()));
            }

            document.add(table);
            Paragraph sign = new Paragraph("\n\nChữ ký giáo viên: ____________________", metaLabelFont);
            sign.setAlignment(Element.ALIGN_RIGHT);
            document.add(sign);

            document.close();
            return out.toByteArray();
        }
    }

    private void addMetaRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell left = new PdfPCell(new Phrase(label + ":", labelFont));
        left.setBorder(PdfPCell.NO_BORDER);
        left.setPaddingBottom(4f);
        PdfPCell right = new PdfPCell(new Phrase(value, valueFont));
        right.setBorder(PdfPCell.NO_BORDER);
        right.setPaddingBottom(4f);
        table.addCell(left);
        table.addCell(right);
    }

    private void addTableHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        cell.setBackgroundColor(new Color(255, 127, 80));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font, Color background, Color fontColor) {
        Font effectiveFont = fontColor == null
                ? font
                : FontFactory.getFont(FontFactory.HELVETICA, 10, fontColor);
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, effectiveFont));
        cell.setPadding(6f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(background);
        table.addCell(cell);
    }

    private String formatMssv(String raw) {
        if (raw == null || raw.isBlank()) return "HS-0000";
        String digits = raw.replaceAll("\\D", "");
        if (digits.isBlank()) return "HS-0000";
        int id = Integer.parseInt(digits);
        return String.format("HS-%04d", id);
    }

    private String mapDataTypesVi(List<String> dataTypes) {
        if (dataTypes == null || dataTypes.isEmpty()) return "-";
        return dataTypes.stream()
                .map(type -> switch (type.toUpperCase(Locale.ROOT)) {
                    case "GRADEBOOK", "GRADES" -> "Bảng điểm";
                    case "ATTENDANCE" -> "Điểm danh";
                    case "PROGRESS" -> "Tiến độ học tập";
                    default -> type;
                })
                .collect(Collectors.joining(", "));
    }

    private String mapTimeRangeVi(String timeRange) {
        if (timeRange == null) return "-";
        return switch (timeRange.toUpperCase(Locale.ROOT)) {
            case "THIS_WEEK" -> "Tuần hiện tại";
            case "THIS_MONTH" -> "Tháng hiện tại";
            case "CURRENT_SEMESTER", "CURRENT_SEM" -> "Học kỳ hiện tại";
            default -> timeRange;
        };
    }

    private String mapFormatVi(String format) {
        if (format == null) return "-";
        return switch (format.toUpperCase(Locale.ROOT)) {
            case "PDF" -> "PDF";
            case "CSV" -> "CSV";
            case "EXCEL" -> "Excel";
            default -> format;
        };
    }

    private String mapStatusVi(String status) {
        if (status == null) return "-";
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "ONLINE" -> "Online";
            case "ATTENTION" -> "Cần chú ý";
            case "OFFLINE" -> "Offline";
            default -> status;
        };
    }

    private String mapMissingLabel(Integer missingCount) {
        if (missingCount == null || missingCount <= 0) {
            return "-";
        }
        return "KHÔNG NỘP (" + missingCount + ")";
    }

    private BaseFont createUnicodeBaseFont() {
        try {
            return BaseFont.createFont("C:/Windows/Fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception first) {
            try {
                return BaseFont.createFont("arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception second) {
                try {
                    return BaseFont.createFont(BaseFont.HELVETICA, "Cp1258", BaseFont.NOT_EMBEDDED);
                } catch (Exception fallback) {
                    throw new RuntimeException("Khong the tao font Unicode cho PDF", fallback);
                }
            }
        }
    }

    private Color mapStatusColor(String status) {
        if (status == null) return null;
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "ONLINE" -> new Color(0, 153, 102);
            case "ATTENTION" -> new Color(220, 53, 69);
            default -> Color.BLACK;
        };
    }

    private static class ReportPdfPageEvent extends PdfPageEventHelper {
        private PdfTemplate totalPages;
        private Font footerFont;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPages = writer.getDirectContent().createTemplate(30, 16);
            footerFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            String text = "Page " + writer.getPageNumber() + "/";
            float x = document.right() - 90;
            float y = document.bottom() - 16;
            cb.beginText();
            cb.setFontAndSize(footerFont.getBaseFont(), footerFont.getSize());
            cb.setTextMatrix(x, y);
            cb.showText(text);
            cb.endText();
            cb.addTemplate(totalPages, x + 32, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            totalPages.beginText();
            totalPages.setFontAndSize(footerFont.getBaseFont(), footerFont.getSize());
            totalPages.setTextMatrix(0, 0);
            totalPages.showText(String.valueOf(writer.getPageNumber() - 1));
            totalPages.endText();
        }
    }

    private LocalDateTime[] resolveTimeRange(String timeRange) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;

        switch (timeRange.toUpperCase(Locale.ROOT)) {
            case "THIS_WEEK" -> startDate = today.with(DayOfWeek.MONDAY);
            case "THIS_MONTH" -> startDate = today.withDayOfMonth(1);
            default -> startDate = today.minusMonths(6);
        }

        return new LocalDateTime[]{startDate.atStartOfDay(), LocalDateTime.now()};
    }

    private int syncTeacherForClassTimetables(Integer classID, Teacher teacher) {
        List<Timetable> timetables = timetableRepository.findByClassroom_ClassID(classID);
        if (timetables.isEmpty()) {
            return 0;
        }

        int changedCount = 0;
        for (Timetable timetable : timetables) {
            Integer currentId = timetable.getTeacher() != null ? timetable.getTeacher().getTeacherID() : null;
            Integer nextId = teacher != null ? teacher.getTeacherID() : null;
            if (!Objects.equals(currentId, nextId)) {
                timetable.setTeacher(teacher);
                changedCount++;
            }
        }

        if (changedCount > 0) {
            timetableRepository.saveAll(timetables);
        }

        return changedCount;
    }

    private String buildTeacherChangedContent(Teacher oldTeacher, Teacher newTeacher, int affectedSessions) {
        String newTeacherName = newTeacher != null ? newTeacher.getFullName() : null;
        String oldTeacherName = oldTeacher != null ? oldTeacher.getFullName() : null;

        StringBuilder content = new StringBuilder();
        if (oldTeacherName != null && newTeacherName != null) {
            content.append("Giáo viên đã được thay đổi từ GV ")
                    .append(oldTeacherName)
                    .append(" sang GV ")
                    .append(newTeacherName);
        } else if (newTeacherName != null) {
            content.append("Lớp học của bạn hiện do GV ")
                    .append(newTeacherName)
                    .append(" phụ trách");
        } else {
            content.append("Giáo viên phụ trách lớp học đã được cập nhật");
        }

        if (affectedSessions > 0) {
            content.append("\nĐã cập nhật cho ").append(affectedSessions).append(" buổi học trong lịch");
        }

        return content.toString();
    }
}
