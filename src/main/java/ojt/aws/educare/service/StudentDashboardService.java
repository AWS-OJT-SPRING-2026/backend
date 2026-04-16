package ojt.aws.educare.service;

import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.StudentDashboardResponse;

public interface StudentDashboardService {
    ApiResponse<StudentDashboardResponse> getMyDashboard();
}
