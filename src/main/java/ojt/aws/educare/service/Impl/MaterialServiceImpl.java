package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.response.*;
import ojt.aws.educare.entity.Book;
import ojt.aws.educare.entity.ClassMember;
import ojt.aws.educare.entity.MaterialType;
import ojt.aws.educare.entity.Student;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.repository.BookRepository;
import ojt.aws.educare.repository.ClassMemberRepository;
import ojt.aws.educare.repository.ClassroomMaterialRepository;
import ojt.aws.educare.repository.StudentRepository;
import ojt.aws.educare.repository.UserRepository;
import ojt.aws.educare.service.MaterialService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaterialServiceImpl implements MaterialService {
    StudentRepository studentRepository;
    ClassMemberRepository classMemberRepository;
    ClassroomMaterialRepository classroomMaterialRepository;
    BookRepository bookRepository;
    UserRepository userRepository;
    JdbcTemplate jdbcTemplate;

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

        boolean canAccess = !classIds.isEmpty() && classroomMaterialRepository
                .existsByClassroom_ClassIDInAndBook_IdAndType(classIds, bookId, MaterialType.THEORY);
        if (!canAccess) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        BookHierarchyResponse response = mapBookHierarchyBySql(book);
        return ApiResponse.success("Lấy cấu trúc tài liệu đầy đủ thành công", response);
    }

    private BookHierarchyResponse mapBookHierarchyBySql(Book book) {
        String sql = """
                SELECT c.id AS chapter_id,
                       c.chapter_number,
                       c.title AS chapter_title,
                       l.id AS lesson_id,
                       l.lesson_number,
                       l.title AS lesson_title,
                       l.estimated_time,
                       s.id AS section_id,
                       s.section_number,
                       s.section_title,
                       ss.id AS subsection_id,
                       ss.subsection_number,
                       ss.subsection_title,
                       cb.id AS content_block_id,
                       cb.content AS content_text
                FROM chapters c
                LEFT JOIN lessons l ON l.chapter_id = c.id
                LEFT JOIN sections s ON s.lesson_id = l.id
                LEFT JOIN subsections ss ON ss.section_id = s.id
                LEFT JOIN content_blocks cb ON cb.subsection_id = ss.id
                WHERE c.book_id = ?
                ORDER BY c.chapter_number, l.lesson_number, s.section_number, ss.subsection_number, cb.id
                """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, book.getId());

        BookHierarchyResponse response = BookHierarchyResponse.builder()
                .id(book.getId())
                .bookName(book.getBookName())
                .subjectId(book.getSubject() != null ? book.getSubject().getSubjectID() : null)
                .subjectName(book.getSubject() != null ? book.getSubject().getSubjectName() : null)
                .chapters(new ArrayList<>())
                .build();

        Map<Integer, ChapterResponse> chapterMap = new LinkedHashMap<>();
        Map<Integer, LessonResponse> lessonMap = new LinkedHashMap<>();
        Map<Integer, SectionResponse> sectionMap = new LinkedHashMap<>();
        Map<Integer, SubsectionResponse> subsectionMap = new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {
            Integer chapterId = toInt(row.get("chapter_id"));
            if (chapterId == null) {
                continue;
            }

            ChapterResponse chapter = chapterMap.get(chapterId);
            if (chapter == null) {
                chapter = ChapterResponse.builder()
                        .id(chapterId)
                        .chapterNumber(toStr(row.get("chapter_number")))
                        .title(toStr(row.get("chapter_title")))
                        .lessons(new ArrayList<>())
                        .build();
                chapterMap.put(chapterId, chapter);
                response.getChapters().add(chapter);
            }

            Integer lessonId = toInt(row.get("lesson_id"));
            if (lessonId == null) {
                continue;
            }

            LessonResponse lesson = lessonMap.get(lessonId);
            if (lesson == null) {
                lesson = LessonResponse.builder()
                        .id(lessonId)
                        .lessonNumber(toStr(row.get("lesson_number")))
                        .title(toStr(row.get("lesson_title")))
                        .estimatedTime(toInt(row.get("estimated_time")))
                        .sections(new ArrayList<>())
                        .build();
                lessonMap.put(lessonId, lesson);
                chapter.getLessons().add(lesson);
            }

            Integer sectionId = toInt(row.get("section_id"));
            if (sectionId == null) {
                continue;
            }

            SectionResponse section = sectionMap.get(sectionId);
            if (section == null) {
                section = SectionResponse.builder()
                        .id(sectionId)
                        .sectionNumber(toStr(row.get("section_number")))
                        .sectionTitle(toStr(row.get("section_title")))
                        .subsections(new ArrayList<>())
                        .build();
                sectionMap.put(sectionId, section);
                lesson.getSections().add(section);
            }

            Integer subsectionId = toInt(row.get("subsection_id"));
            if (subsectionId == null) {
                continue;
            }

            SubsectionResponse subsection = subsectionMap.get(subsectionId);
            if (subsection == null) {
                subsection = SubsectionResponse.builder()
                        .id(subsectionId)
                        .subsectionNumber(toStr(row.get("subsection_number")))
                        .subsectionTitle(toStr(row.get("subsection_title")))
                        .contentBlocks(new ArrayList<>())
                        .build();
                subsectionMap.put(subsectionId, subsection);
                section.getSubsections().add(subsection);
            }

            Integer contentBlockId = toInt(row.get("content_block_id"));
            if (contentBlockId != null) {
                subsection.getContentBlocks().add(
                        ContentBlockResponse.builder()
                                .id(contentBlockId)
                                .content(toStr(row.get("content_text")))
                                .build()
                );
            }
        }

        return response;
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }

    private String toStr(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Student getCurrentStudent() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

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
