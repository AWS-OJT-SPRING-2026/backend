package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.ClassroomCreateRequest;
import ojt.aws.educare.dto.request.ClassroomUpdateRequest;
import ojt.aws.educare.dto.response.*;
import ojt.aws.educare.dto.response.TeacherClassroomOptionResponse;
import ojt.aws.educare.entity.*;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.ClassMemberMapper;
import ojt.aws.educare.mapper.ClassroomMapper;
import ojt.aws.educare.repository.*;
import ojt.aws.educare.service.ClassroomService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    ClassroomMapper classroomMapper;
    ClassMemberMapper classMemberMapper;

    @Override
    public ApiResponse<PageResponse<ClassroomResponse>> getAllClassrooms(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Classroom> classroomPage = classroomRepository.findAll(pageable);

        List<ClassroomResponse> responses = classroomMapper.toClassroomResponseList(classroomPage.getContent());

        PageResponse<ClassroomResponse> pageResponse = PageResponse.<ClassroomResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(classroomPage.getTotalPages())
                .totalElements(classroomPage.getTotalElements())
                .data(responses)
                .build();

        return ApiResponse.success("Lấy danh sách lớp học thành công", pageResponse);
    }

    @Override
    public ApiResponse<ClassroomStatsResponse> getClassroomStats() {
        long totalClasses = classroomRepository.count();
        long activeClasses = classroomRepository.countByStatus("ACTIVE");
        long unassignedClasses = classroomRepository.countByTeacherIsNull();
        long totalEnrolledStudents = classMemberRepository.count();
        int avgClassSize = totalClasses == 0 ? 0 : (int) Math.round((double) totalEnrolledStudents / totalClasses);

        ClassroomStatsResponse stats = new ClassroomStatsResponse(totalClasses, activeClasses, unassignedClasses, avgClassSize);
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

        if (teacherID == null) {
            classroom.setTeacher(null);
            classroomRepository.save(classroom);
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

        return ApiResponse.success(message, null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> addStudentsToClass(Integer classID, List<Integer> studentIDs) {
        Classroom classroom = classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        // 2. Lấy danh sách ID học sinh HIỆN TẠI đang có trong lớp từ DB (không dùng cache Hibernate)
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
        return ApiResponse.success("Lấy thông tin lớp học thành công", response);
    }

    @Override
    @Transactional
    public ApiResponse<ClassroomResponse> updateClassroom(Integer classID, ClassroomUpdateRequest request) {
        Classroom classroom = classroomRepository.findById(classID)
                .orElseThrow(() -> new AppException(ErrorCode.CLASSROOM_NOT_FOUND));

        if (request.getSubjectID() != null && (classroom.getSubject() == null || !classroom.getSubject().getSubjectID().equals(request.getSubjectID()))) {
            Subject subject = subjectRepository.findById(request.getSubjectID())
                    .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));
            classroom.setSubject(subject);
        }

        if (request.getTeacherID() == null) {
            classroom.setTeacher(null);
        } else if (classroom.getTeacher() == null || !classroom.getTeacher().getTeacherID().equals(request.getTeacherID())) {
            Teacher teacher = teacherRepository.findById(request.getTeacherID())
                    .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));
            classroom.setTeacher(teacher);
        }

        classroomMapper.updateClassroomFromRequest(classroom, request);

        Classroom savedClassroom = classroomRepository.save(classroom);
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
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Teacher teacher = teacherRepository.findByUser(currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.USER_IS_NOT_TEACHER));

        List<Classroom> classrooms = classroomRepository.findByTeacher_TeacherIDOrderByClassNameAsc(teacher.getTeacherID());
        List<TeacherClassroomOptionResponse> options = classroomMapper.toTeacherClassroomOptionResponseList(classrooms);

        return ApiResponse.success("Lấy danh sách lớp học của giáo viên thành công", options);
    }
}
