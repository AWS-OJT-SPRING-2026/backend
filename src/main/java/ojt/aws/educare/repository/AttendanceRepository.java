package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    Optional<Attendance> findByTimetable_TimetableIDAndStudent_StudentID(Integer timetableID, Integer studentID);
    List<Attendance> findByTimetable_TimetableID(Integer timetableID);
}