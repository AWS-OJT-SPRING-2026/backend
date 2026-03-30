package ojt.aws.educare.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.BookHierarchyResponse;
import ojt.aws.educare.dto.response.StudentTheorySubjectOverviewResponse;
import ojt.aws.educare.service.MaterialService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/students/me/materials")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentMaterialController {
    MaterialService materialService;

    @GetMapping("/theory/subjects")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<List<StudentTheorySubjectOverviewResponse>> getTheorySubjectsOverview() {
        return materialService.getMyTheorySubjectsOverview();
    }

    @GetMapping("/theory/books/{bookId}/full-hierarchy")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<BookHierarchyResponse> getTheoryBookFullHierarchy(@PathVariable Integer bookId) {
        return materialService.getMyTheoryBookFullHierarchy(bookId);
    }
}

