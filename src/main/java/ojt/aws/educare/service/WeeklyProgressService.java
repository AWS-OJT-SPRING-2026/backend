package ojt.aws.educare.service;

import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.WeeklyProgressResponse;

import java.time.LocalDate;
import java.time.YearMonth;

public interface WeeklyProgressService {
    ApiResponse<WeeklyProgressResponse> getMyWeeklyProgress();

    ApiResponse<WeeklyProgressResponse> getMyProgress(String type, LocalDate startDate, YearMonth month);
}
