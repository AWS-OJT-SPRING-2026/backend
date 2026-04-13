package ojt.aws.educare.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.StudentNoteRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.StudentNoteResponse;
import ojt.aws.educare.service.StudentNoteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentNoteController {

    StudentNoteService studentNoteService;

    @GetMapping("/notes")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<StudentNoteResponse> getMyNote(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return studentNoteService.getMyNote(date);
    }

    @PostMapping("/notes")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResponse<StudentNoteResponse> saveMyNote(@Valid @RequestBody StudentNoteRequest request) {
        return studentNoteService.saveMyNote(request);
    }
}

