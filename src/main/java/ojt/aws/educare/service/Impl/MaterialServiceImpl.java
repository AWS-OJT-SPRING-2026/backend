package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.configuration.CurrentUserProvider;
import ojt.aws.educare.dto.response.*;
import ojt.aws.educare.entity.Book;
import ojt.aws.educare.entity.ClassMember;
import ojt.aws.educare.entity.MaterialType;
import ojt.aws.educare.entity.Student;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.mapper.BookHierarchyMapper;
import ojt.aws.educare.repository.BookRepository;
import ojt.aws.educare.repository.ClassMemberRepository;
import ojt.aws.educare.repository.ClassroomMaterialRepository;
import ojt.aws.educare.repository.StudentRepository;
import ojt.aws.educare.service.MaterialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaterialServiceImpl implements MaterialService {
    StudentRepository studentRepository;
    ClassMemberRepository classMemberRepository;
    ClassroomMaterialRepository classroomMaterialRepository;
    BookRepository bookRepository;
    CurrentUserProvider currentUserProvider;
    BookHierarchyMapper bookHierarchyMapper;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<StudentTheorySubjectOverviewResponse>> getMyTheorySubjectsOverview() {
        Student student = getCurrentStudent();
        Set<Integer> classIds = getStudentClassIds(student.getStudentID());
        if (classIds.isEmpty()) {
            return ApiResponse.success("Học sinh không thuộc lớp nào", List.of());
        }

        List<StudentTheorySubjectOverviewResponse> result = classroomMaterialRepository
                .summarizeTheoryBySubjectForStudent(classIds, MaterialType.THEORY);

        return ApiResponse.success("Lấy tổng quan tài liệu lý thuyết theo môn thành công", result);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<BookHierarchyResponse> getMyTheoryBookFullHierarchy(Integer bookId) {
        Student student = getCurrentStudent();
        Set<Integer> classIds = getStudentClassIds(student.getStudentID());

        boolean canAccess = !classIds.isEmpty()
                && classroomMaterialRepository.existsTheoryBookForClasses(classIds, bookId);
        if (!canAccess) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        BookHierarchyResponse response = bookHierarchyMapper.toBookHierarchyResponse(book);
        return ApiResponse.success("Lấy cấu trúc tài liệu đầy đủ thành công", response);
    }


    private Student getCurrentStudent() {
        User user = currentUserProvider.getCurrentUser();

        return studentRepository.findByUser_UserID(user.getUserID())
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
    }

    private Set<Integer> getStudentClassIds(Integer studentId) {
        List<ClassMember> classMembers = classMemberRepository.findByStudent_StudentID(studentId);
        return classMembers.stream()
                .map(cm -> cm.getClassroom().getClassID())
                .collect(Collectors.toSet());
    }
}
