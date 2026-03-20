package ojt.aws.educare.service;

import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.TeacherResponse;

import java.util.List;

public interface TeacherService {
    ApiResponse<List<TeacherResponse>> getAllTeachers();
}
