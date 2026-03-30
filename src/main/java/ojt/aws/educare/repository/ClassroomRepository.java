package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Classroom;
import ojt.aws.educare.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Integer> {
    Optional<Classroom> findByClassNameAndTeacher(String className, Teacher teacher);
    boolean existsByClassNameAndTeacher(String className, Teacher teacher);
    Optional<Classroom> findByClassName(String classname);
    List<Classroom> findByTeacher_TeacherIDOrderByClassNameAsc(Integer teacherID);
    long countByStatus(String status);
    long countByTeacherIsNull();
}