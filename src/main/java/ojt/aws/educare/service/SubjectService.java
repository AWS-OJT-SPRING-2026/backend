package ojt.aws.educare.service;

import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.SubjectResponse;

import java.util.List;

public interface SubjectService {
    ApiResponse<List<SubjectResponse>> getAllSubjects();
}