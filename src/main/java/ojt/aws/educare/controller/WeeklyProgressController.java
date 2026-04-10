package ojt.aws.educare.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.WeeklyProgressResponse;
import ojt.aws.educare.service.WeeklyProgressService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WeeklyProgressController {

    WeeklyProgressService weeklyProgressService;

    @GetMapping("/progress-week")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<WeeklyProgressResponse> getMyWeeklyProgress() {
        return weeklyProgressService.getMyWeeklyProgress();
    }
}
