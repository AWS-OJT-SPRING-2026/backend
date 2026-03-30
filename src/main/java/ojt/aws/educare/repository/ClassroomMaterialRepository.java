package ojt.aws.educare.repository;

import ojt.aws.educare.entity.ClassroomMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassroomMaterialRepository extends JpaRepository<ClassroomMaterial, Integer> {
    List<ClassroomMaterial> findByBook_IdAndType(Integer bookId, String type);
    List<ClassroomMaterial> findByQuestionBank_IdAndType(Integer questionBankId, String type);
}

