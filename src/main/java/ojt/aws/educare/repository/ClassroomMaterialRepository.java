package ojt.aws.educare.repository;

import ojt.aws.educare.dto.response.StudentTheorySubjectOverviewResponse;
import ojt.aws.educare.entity.ClassroomMaterial;
import ojt.aws.educare.entity.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ClassroomMaterialRepository extends JpaRepository<ClassroomMaterial, Integer> {
    List<ClassroomMaterial> findByBook_IdAndType(Integer bookId, MaterialType type);
    List<ClassroomMaterial> findByQuestionBank_IdAndType(Integer questionBankId, MaterialType type);

    boolean existsByClassroom_ClassIDInAndBook_IdAndType(Collection<Integer> classIds, Integer bookId, MaterialType type);

    @Query("""
            SELECT new ojt.aws.educare.dto.response.StudentTheorySubjectOverviewResponse(
                s.subjectID,
                s.subjectName,
                MIN(b.id),
                COUNT(DISTINCT c.id),
                COUNT(DISTINCT l.id)
            )
            FROM ClassroomMaterial cm
            JOIN cm.classroom cls
            JOIN cm.book b
            JOIN b.subject s
            LEFT JOIN Chapter c ON c.book = b
            LEFT JOIN Lesson l ON l.chapter = c
            WHERE cls.classID IN :classIds
              AND cm.type = :type
            GROUP BY s.subjectID, s.subjectName
            ORDER BY s.subjectName ASC
            """)
    List<StudentTheorySubjectOverviewResponse> summarizeTheoryBySubjectForStudent(
            @Param("classIds") Collection<Integer> classIds,
            @Param("type") MaterialType type
    );
}
