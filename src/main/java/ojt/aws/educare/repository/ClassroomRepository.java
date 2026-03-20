package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Classroom;
import ojt.aws.educare.entity.Student;
import ojt.aws.educare.entity.Teacher;
import ojt.aws.educare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Integer> {
    Optional<Classroom> findByClassNameAndTeacher(String className, Teacher teacher);
    boolean existsByClassNameAndTeacher(String className, Teacher teacher);
    Optional<Classroom> findByClassName(String classname);
    long countByStatus(String status);
    long countByTeacherIsNull();
}