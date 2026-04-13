package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.StudentNoteRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.StudentNoteResponse;

import java.time.LocalDate;

public interface StudentNoteService {
    ApiResponse<StudentNoteResponse> getMyNote(LocalDate date);
    ApiResponse<StudentNoteResponse> saveMyNote(StudentNoteRequest request);
}

