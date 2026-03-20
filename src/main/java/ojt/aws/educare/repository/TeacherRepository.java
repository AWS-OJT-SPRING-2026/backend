package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Teacher;
import ojt.aws.educare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    Optional<Teacher> findByUser(User user);
    Optional<Teacher> findByUser_UserID(Integer userID);
    boolean existsByUser(User user);
}