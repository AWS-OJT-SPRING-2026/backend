package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Student;
import ojt.aws.educare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByUser(User user);
    Optional<Student> findByUser_UserID(Integer userID);
    Optional<Student> findByUser_Username(String username);
    boolean existsByUser(User user);
}