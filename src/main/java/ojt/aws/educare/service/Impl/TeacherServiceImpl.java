package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.TeacherResponse;
import ojt.aws.educare.entity.Teacher;
import ojt.aws.educare.mapper.UserMapper;
import ojt.aws.educare.repository.TeacherRepository;
import ojt.aws.educare.service.TeacherService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeacherServiceImpl implements TeacherService {
    TeacherRepository teacherRepository;
    UserMapper userMapper;

    @Override
    public ApiResponse<List<TeacherResponse>> getAllTeachers() {
        List<Teacher> teachers = teacherRepository.findAll();

        List<TeacherResponse> teacherResponses = userMapper.toTeacherResponseList(teachers);

        return ApiResponse.success("Lấy danh sách giáo viên thành công", teacherResponses);
    }
}
