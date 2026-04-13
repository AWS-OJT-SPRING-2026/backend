package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.configuration.CurrentUserProvider;
import ojt.aws.educare.dto.request.StudentNoteRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.StudentNoteResponse;
import ojt.aws.educare.entity.Student;
import ojt.aws.educare.entity.StudentNote;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.StudentNoteMapper;
import ojt.aws.educare.repository.StudentNoteRepository;
import ojt.aws.educare.repository.StudentRepository;
import ojt.aws.educare.service.StudentNoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StudentNoteServiceImpl implements StudentNoteService {

    CurrentUserProvider currentUserProvider;
    StudentRepository studentRepository;
    StudentNoteRepository studentNoteRepository;
    StudentNoteMapper studentNoteMapper;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<StudentNoteResponse> getMyNote(LocalDate date) {
        Student student = getCurrentStudent();

        StudentNoteResponse response = studentNoteRepository
                .findByStudent_StudentIDAndNoteDate(student.getStudentID(), date)
                .map(studentNoteMapper::toResponse)
                .orElse(StudentNoteResponse.builder()
                        .date(date)
                        .content("")
                        .build());

        return ApiResponse.<StudentNoteResponse>builder().result(response).build();
    }

    @Override
    @Transactional
    public ApiResponse<StudentNoteResponse> saveMyNote(StudentNoteRequest request) {
        Student student = getCurrentStudent();

        StudentNote note = studentNoteRepository
                .findByStudent_StudentIDAndNoteDate(student.getStudentID(), request.getDate())
                .orElseGet(() -> {
                    StudentNote newNote = studentNoteMapper.toStudentNote(request);
                    newNote.setStudent(student);
                    return newNote;
                });

        note.setContent(request.getContent() == null ? "" : request.getContent());
        StudentNote saved = studentNoteRepository.save(note);

        return ApiResponse.<StudentNoteResponse>builder()
                .result(studentNoteMapper.toResponse(saved))
                .build();
    }

    private Student getCurrentStudent() {
        User currentUser = currentUserProvider.getCurrentUser();
        return studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
    }
}

