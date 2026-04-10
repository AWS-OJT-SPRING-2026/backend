package ojt.aws.educare.service;

import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.WeeklyProgressResponse;

public interface WeeklyProgressService {
    ApiResponse<WeeklyProgressResponse> getMyWeeklyProgress();
}
