package ojt.aws.educare.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.WeeklyProgressResponse;
import ojt.aws.educare.service.WeeklyProgressService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

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

    @GetMapping("/progress")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<WeeklyProgressResponse> getMyProgress(
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return weeklyProgressService.getMyProgress(type, startDate, month);
    }
}
