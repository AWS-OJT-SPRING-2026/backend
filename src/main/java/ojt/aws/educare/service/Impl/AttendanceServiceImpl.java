package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import ojt.aws.educare.dto.request.AttendanceRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.AttendanceStudentResponse;
import ojt.aws.educare.entity.*;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.AttendanceMapper;
import ojt.aws.educare.repository.AttendanceRepository;
import ojt.aws.educare.repository.StudentRepository;
import ojt.aws.educare.repository.TimetableRepository;
import ojt.aws.educare.repository.UserRepository;
import ojt.aws.educare.service.AttendanceService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {
    TimetableRepository timetableRepository;
    UserRepository userRepository;
    AttendanceRepository attendanceRepository;
    StudentRepository studentRepository;

    AttendanceMapper  attendanceMapper;

    @Override
    @Transactional
    public ApiResponse<Void> saveAttendance(Integer timetableID, List<AttendanceRequest> requests) {
        Timetable timetable = timetableRepository.findById(timetableID)
                .orElseThrow(() -> new AppException(ErrorCode.TIMETABLE_NOT_FOUND));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String role = currentUser.getRole().getRoleName().toUpperCase();

        if ("TEACHER".equals(role)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deadline = timetable.getEndTime().plusHours(24);

            if (now.isBefore(timetable.getStartTime())) {
                throw new RuntimeException("Lỗi: Chưa đến giờ bắt đầu buổi học, không thể điểm danh!");
            }
            if (now.isAfter(deadline)) {
                throw new RuntimeException("Lỗi: Đã quá hạn 24h để chỉnh sửa điểm danh. Vui lòng liên hệ Admin!");
            }
        }

        for (AttendanceRequest request : requests) {
            Student student = studentRepository.findById(request.getStudentID())
                    .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

            // Tìm xem record điểm danh của học sinh này trong buổi này đã tồn tại chưa
            Attendance attendance = attendanceRepository
                    .findByTimetable_TimetableIDAndStudent_StudentID(timetableID, request.getStudentID())
                    .orElse(null);

            if (attendance != null) {
                attendanceMapper.updateAttendanceFromRequest(attendance, request);
            } else {
                attendance = attendanceMapper.toAttendance(request, timetable, student);
            }
            attendanceRepository.save(attendance);
        }

        return ApiResponse.success("Lưu dữ liệu điểm danh thành công", null);
    }

    @Override
    public ApiResponse<List<AttendanceStudentResponse>> getAttendanceByTimetable(Integer timetableID) {
        Timetable timetable = timetableRepository.findById(timetableID)
                .orElseThrow(() -> new AppException(ErrorCode.TIMETABLE_NOT_FOUND));

        List<ClassMember> activeMembers = timetable.getClassroom().getClassMembers().stream()
                .filter(member -> "ACTIVE".equalsIgnoreCase(member.getStatus()))
                .toList();

        List<Attendance> savedAttendances = attendanceRepository.findByTimetable_TimetableID(timetableID);

        List<AttendanceStudentResponse> responseList = activeMembers.stream().map(member -> {
            Student student = member.getStudent();

            // Tìm record điểm danh (trả về null nếu chưa có)
            Attendance attendanceRecord = savedAttendances.stream()
                    .filter(a -> a.getStudent().getStudentID().equals(student.getStudentID()))
                    .findFirst()
                    .orElse(null);

            return attendanceMapper.toAttendanceStudentResponse(student, attendanceRecord);

        }).toList();

        return ApiResponse.success("Lấy danh sách điểm danh thành công", responseList);
    }
}
