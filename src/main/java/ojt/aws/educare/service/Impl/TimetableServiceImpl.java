package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.*;
import ojt.aws.educare.dto.response.*;
import ojt.aws.educare.entity.*;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.TimetableMapper;
import ojt.aws.educare.repository.*;
import ojt.aws.educare.service.TimetableService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TimetableServiceImpl implements TimetableService {

    TimetableRepository timetableRepository;
    ClassroomRepository classroomRepository;
    TeacherRepository teacherRepository;
    SubjectRepository subjectRepository;
    UserRepository userRepository;

    TimetableMapper timetableMapper;

    // TẠO 1 BUỔI ĐƠN LẺ
    @Override
    @Transactional
    public ApiResponse<TimetableResponse> createSingleTimetable(TimetableRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());

        Classroom classroom = getClassroom(request.getClassID());

        if (classroom.getSubject() == null) {
            throw new AppException(ErrorCode.CLASSROOM_NO_SUBJECT);
        }

        Teacher teacher = resolveTeacher(request.getTeacherID(), classroom);

        Timetable timetable = timetableMapper.toTimetable(request);
        timetable.setClassroom(classroom);
        timetable.setTeacher(teacher);

        Timetable saved = timetableRepository.save(timetable);
        return ApiResponse.success("Tạo buổi học thành công", timetableMapper.toResponse(saved));
    }

    // TẠO ĐỊNH KỲ LẶP LẠI
    @Override
    @Transactional
    public ApiResponse<Void> createRecurringTimetable(TimetableRecurringRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(ErrorCode.TIMETABLE_DATE_INVALID);
        }
        validateTimeRange(request.getStartTime(), request.getEndTime());
        if (request.getDaysOfWeek() == null || request.getDaysOfWeek().isEmpty()) {
            throw new AppException(ErrorCode.TIMETABLE_DATE_INVALID);
        }

        Classroom classroom = getClassroom(request.getClassID());

        if (classroom.getSubject() == null) {
            throw new AppException(ErrorCode.CLASSROOM_NO_SUBJECT);
        }

        Teacher teacher = resolveTeacher(request.getTeacherID(), classroom);

        List<Timetable> newTimetables = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();

        // Chạy vòng lặp từ ngày bắt đầu đến ngày kết thúc
        while (!currentDate.isAfter(request.getEndDate())) {
            // Kiểm tra xem ngày hiện tại có nằm trong list các "Thứ" mà user đã chọn không
            if (request.getDaysOfWeek().contains(currentDate.getDayOfWeek())) {

                // Ghép ngày với giờ để tạo thành LocalDateTime
                LocalDateTime startDateTime = LocalDateTime.of(currentDate, request.getStartTime());
                LocalDateTime endDateTime = LocalDateTime.of(currentDate, request.getEndTime());

                Timetable session = Timetable.builder()
                        .classroom(classroom)
                        .teacher(teacher)
                        .topic(request.getTopic())
                        .googleMeetLink(request.getGoogleMeetLink())
                        .startTime(startDateTime)
                        .endTime(endDateTime)
                        .status(TimetableStatus.SCHEDULED)
                        .build();

                newTimetables.add(session);
            }
            currentDate = currentDate.plusDays(1); // Tăng lên 1 ngày
        }

        timetableRepository.saveAll(newTimetables);
        return ApiResponse.success("Đã tạo thành công " + newTimetables.size() + " buổi học lặp lại", null);
    }

    // CHỈNH SỬA TẤT CẢ BUỔI CỦA 1 LỚP
    @Override
    @Transactional
    public ApiResponse<Void> bulkUpdateTimetable(Integer classID, TimetableBulkUpdateRequest request) {
        Classroom classroom = classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        List<Timetable> timetables = timetableRepository.findByClassroom_ClassID(classID);

        Teacher newTeacher = request.getTeacherID() != null ?
                teacherRepository.findById(request.getTeacherID())
                        .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND)) : null;

        if (request.getStartTime() != null && request.getEndTime() != null) {
            validateTimeRange(request.getStartTime(), request.getEndTime());
        }

        if (request.getStartDate() != null && request.getEndDate() != null && request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(ErrorCode.TIMETABLE_DATE_INVALID);
        }

        if (request.getSubjectID() != null && (classroom.getSubject() == null || !request.getSubjectID().equals(classroom.getSubject().getSubjectID()))) {
            Subject subject = subjectRepository.findById(request.getSubjectID())
                    .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
            classroom.setSubject(subject);
        }

        if (request.getStartDate() != null) {
            classroom.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            classroom.setEndDate(request.getEndDate());
        }

        classroomRepository.save(classroom);

        for (Timetable t : timetables) {
            if (request.getTopic() != null) t.setTopic(request.getTopic());
            if (request.getGoogleMeetLink() != null) t.setGoogleMeetLink(request.getGoogleMeetLink());
            if (newTeacher != null) t.setTeacher(newTeacher);

            // Cập nhật lại giờ (giữ nguyên ngày cũ, chỉ đổi LocalTime)
            LocalDateTime nextStart = t.getStartTime();
            LocalDateTime nextEnd = t.getEndTime();
            if (request.getStartTime() != null) {
                nextStart = LocalDateTime.of(t.getStartTime().toLocalDate(), request.getStartTime());
            }
            if (request.getEndTime() != null) {
                nextEnd = LocalDateTime.of(t.getEndTime().toLocalDate(), request.getEndTime());
            }

            if (!nextEnd.isAfter(nextStart)) {
                throw new AppException(ErrorCode.TIMETABLE_TIME_INVALID);
            }

            t.setStartTime(nextStart);
            t.setEndTime(nextEnd);
        }

        timetableRepository.saveAll(timetables);
        return ApiResponse.success("Đã cập nhật hàng loạt thành công", null);
    }

    @Override
    @Transactional
    public ApiResponse<TimetableResponse> updateSingleTimetable(Integer iD, TimetableRequest request) {
        // Kiểm tra thời gian hợp lệ
        validateTimeRange(request.getStartTime(), request.getEndTime());

        // Tìm buổi học cần sửa
        Timetable timetable = timetableRepository.findById(iD)
                .orElseThrow(() -> new AppException(ErrorCode.TIMETABLE_NOT_FOUND));

        // Cập nhật Lớp học (Trong trường hợp Admin muốn chuyển buổi này sang lớp khác)
        if (request.getClassID() != null && !request.getClassID().equals(timetable.getClassroom().getClassID())) {
            Classroom classroom = getClassroom(request.getClassID());
            timetable.setClassroom(classroom);
        }

        // Cập nhật Giáo viên (Cho phép gỡ giáo viên nếu truyền null, hoặc đổi người khác)
        if (request.getTeacherID() == null) {
            timetable.setTeacher(null);
        } else if (timetable.getTeacher() == null || !timetable.getTeacher().getTeacherID().equals(request.getTeacherID())) {
            Teacher teacher = teacherRepository.findById(request.getTeacherID())
                    .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));
            timetable.setTeacher(teacher);
        }

        // Cập nhật các thông tin còn lại (Topic, Google Meet Link, Giờ bắt đầu, Giờ kết thúc)
        timetableMapper.updateTimetableFromRequest(timetable, request);

        Timetable savedTimetable = timetableRepository.save(timetable);

        return ApiResponse.success("Cập nhật buổi học thành công", timetableMapper.toResponse(savedTimetable));
    }

    // LẤY DANH SÁCH LỊCH CHO CALENDAR & TỰ ĐỘNG CẬP NHẬT TRẠNG THÁI
    @Override
    @Transactional
    public ApiResponse<List<TimetableResponse>> getTimetables(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new AppException(ErrorCode.TIMETABLE_DATE_INVALID);
        }

        List<Timetable> timetables = timetableRepository.findByTimeRange(start, end);

        syncStatuses(timetables, LocalDateTime.now());

        List<TimetableResponse> timetableResponses = timetableMapper.toResponseList(timetables);
        return ApiResponse.success("Lấy danh sách lịch thành công", timetableResponses);
    }

    // THỐNG KÊ
    @Override
    @Transactional
    public ApiResponse<TimetableStatsResponse> getStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        List<Timetable> todayTimetables = timetableRepository.findByTimeRange(startOfDay, endOfDay.plusSeconds(1));
        syncStatuses(todayTimetables, LocalDateTime.now());

        long total = timetableRepository.countByStartTimeBetween(startOfDay, endOfDay);
        long ongoing = timetableRepository.countByStatusAndStartTimeBetween(TimetableStatus.ONGOING, startOfDay, endOfDay);
        long upcoming = timetableRepository.countByStatusAndStartTimeBetween(TimetableStatus.SCHEDULED, startOfDay, endOfDay);
        long completed = timetableRepository.countByStatusAndStartTimeBetween(TimetableStatus.COMPLETED, startOfDay, endOfDay);

        TimetableStatsResponse stats = new TimetableStatsResponse(total, ongoing, upcoming, completed);
        return ApiResponse.success("Lấy thống kê thành công", stats);
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteTimetable(Integer iD) {
        if (!timetableRepository.existsById(iD)) {
            throw new AppException(ErrorCode.TIMETABLE_NOT_FOUND);
        }
        timetableRepository.deleteById(iD);
        return ApiResponse.success("Đã xóa buổi học", null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteAllByClass(Integer classID) {
        classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));
        timetableRepository.deleteByClassroom_ClassID(classID);
        return ApiResponse.success("Đã xóa toàn bộ lịch của lớp học", null);
    }

    @Override
    @Transactional
    public ApiResponse<List<TimetableResponse>> getMyScheduleList(LocalDateTime start, LocalDateTime end) {
        Teacher currentTeacher = getCurrentTeacher();

        List<Timetable> timetables = timetableRepository.findByTeacherAndTimeRange(currentTeacher.getTeacherID(), start, end);

        syncStatuses(timetables, LocalDateTime.now());

        return ApiResponse.success("Lấy danh sách lịch dạy thành công", timetableMapper.toResponseList(timetables));
    }


    @Override
    @Transactional
    public ApiResponse<TimetableResponse> updateMeetLink(Integer timetableID, UpdateMeetLinkRequest request) {
        Timetable timetable = timetableRepository.findById(timetableID)
                .orElseThrow(() -> new AppException(ErrorCode.TIMETABLE_NOT_FOUND));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();

        if (timetable.getTeacher() == null || !timetable.getTeacher().getUser().getUserID().equals(currentUser.getUserID())) {
            throw new AppException(ErrorCode.NO_PERMISSION_UPDATE_TIMETABLE_LINK);
        }

        timetable.setGoogleMeetLink(request.getGoogleMeetLink());
        timetableRepository.save(timetable);

        return ApiResponse.success("Cập nhật Link Google Meet thành công", timetableMapper.toResponse(timetable));
    }

    @Override
    public ApiResponse<TeacherScheduleStatsResponse> getMyScheduleStats(LocalDateTime start, LocalDateTime end) {
        Teacher currentTeacher = getCurrentTeacher();

        List<Timetable> timetables = timetableRepository.findByTeacherAndTimeRange(currentTeacher.getTeacherID(), start, end);

        long total = timetables.size();

        // Dùng Stream API đếm số buổi đã có link
        long hasLink = timetables.stream()
                .filter(t -> t.getGoogleMeetLink() != null && !t.getGoogleMeetLink().trim().isEmpty())
                .count();

        long missingLink = total - hasLink;

        TeacherScheduleStatsResponse stats = new TeacherScheduleStatsResponse(total, hasLink, missingLink);
        return ApiResponse.success("Lấy thống kê thành công", stats);
    }

    private Teacher resolveTeacher(Integer teacherID, Classroom classroom) {
        return teacherID == null
                ? classroom.getTeacher()
                : teacherRepository.findById(teacherID)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));
    }

    private Classroom getClassroom(Integer classID) {
        return classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));
    }

    private void validateTimeRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new AppException(ErrorCode.TIMETABLE_TIME_INVALID);
        }
    }

    private void validateTimeRange(java.time.LocalTime start, java.time.LocalTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new AppException(ErrorCode.TIMETABLE_TIME_INVALID);
        }
    }

    private void syncStatuses(List<Timetable> timetables, LocalDateTime now) {
        boolean changed = false;
        for (Timetable t : timetables) {
            TimetableStatus oldStatus = t.getStatus();
            if (oldStatus != TimetableStatus.CANCELLED) {
                if (now.isBefore(t.getStartTime())) {
                    t.setStatus(TimetableStatus.SCHEDULED);
                } else if (!now.isBefore(t.getStartTime()) && now.isBefore(t.getEndTime())) {
                    t.setStatus(TimetableStatus.ONGOING);
                } else {
                    t.setStatus(TimetableStatus.COMPLETED);
                }
            }
            if (oldStatus != t.getStatus()) {
                changed = true;
            }
        }

        if (changed) {
            timetableRepository.saveAll(timetables);
        }
    }

    private Teacher getCurrentTeacher() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Teacher currentTeacher = currentUser.getTeacher();
        if (currentTeacher == null) {
            throw new AppException(ErrorCode.USER_IS_NOT_TEACHER);
        }
        return currentTeacher;
    }



}