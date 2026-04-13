package ojt.aws.educare.repository;

import ojt.aws.educare.entity.StudentNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StudentNoteRepository extends JpaRepository<StudentNote, Integer> {
    Optional<StudentNote> findByStudent_StudentIDAndNoteDate(Integer studentId, LocalDate noteDate);
}

