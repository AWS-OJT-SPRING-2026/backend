package ojt.aws.educare.service;

import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.BookHierarchyResponse;
import ojt.aws.educare.dto.response.StudentTheorySubjectOverviewResponse;

import java.util.List;

public interface MaterialService {
    ApiResponse<List<StudentTheorySubjectOverviewResponse>> getMyTheorySubjectsOverview();
    ApiResponse<BookHierarchyResponse> getMyTheoryBookFullHierarchy(Integer bookId);
}

