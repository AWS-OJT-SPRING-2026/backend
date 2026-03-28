package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    Optional<Attendance> findByTimetable_TimetableIDAndStudent_StudentID(Integer timetableID, Integer studentID);
    List<Attendance> findByTimetable_TimetableID(Integer timetableID);

    @Query("SELECT a FROM Attendance a " +
            "JOIN a.timetable t " +
            "WHERE a.student.user.username = :username " +
            "AND t.startTime >= :start AND t.endTime <= :end " +
            "AND a.status = 'PRESENT'")
    List<Attendance> findAttendedByStudentUsernameAndTimeRange(
            @Param("username") String username,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Attendance a " +
            "JOIN a.timetable t " +
            "WHERE a.student.user.username = :username " +
            "AND t.startTime >= :start AND t.endTime <= :end")
    List<Attendance> findAllAttendanceByStudentUsernameAndTimeRange(
            @Param("username") String username,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}