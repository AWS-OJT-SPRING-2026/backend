package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.StudentResponse;
import ojt.aws.educare.entity.Student;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.UserMapper;
import ojt.aws.educare.repository.StudentRepository;
import ojt.aws.educare.service.StudentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentServiceImpl implements StudentService {
    StudentRepository studentRepository;
    UserMapper userMapper;

    @Override
    public ApiResponse<List<StudentResponse>> getAllStudents() {
        List<Student> students = studentRepository.findAll();

        List<StudentResponse> studentResponses = userMapper.toStudentResponseList(students);

        return ApiResponse.success("Lấy danh sách học sinh thành công", studentResponses);
    }

    @Override
    public ApiResponse<StudentResponse> getStudentByID(Integer studentID) {
        Student student = studentRepository.findById(studentID)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        StudentResponse response = userMapper.toStudentResponse(student);

        return ApiResponse.success("Lấy thông tin học sinh thành công", response);
    }
}
