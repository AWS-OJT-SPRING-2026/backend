package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.SubjectResponse;
import ojt.aws.educare.entity.Subject;
import ojt.aws.educare.mapper.SubjectMapper;
import ojt.aws.educare.repository.SubjectRepository;
import ojt.aws.educare.service.SubjectService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubjectServiceImpl implements SubjectService {

    SubjectRepository subjectRepository;
    SubjectMapper subjectMapper;

    @Override
    public ApiResponse<List<SubjectResponse>> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAll();

        List<SubjectResponse> responses = subjectMapper.toSubjectResponseList(subjects);

        return ApiResponse.success("Lấy danh sách môn học thành công", responses);
    }
}