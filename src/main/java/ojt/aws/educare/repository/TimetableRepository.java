package ojt.aws.educare.repository;

import ojt.aws.educare.entity.Classroom;
import ojt.aws.educare.entity.Timetable;
import ojt.aws.educare.entity.TimetableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TimetableRepository extends JpaRepository<Timetable, Integer> {
    boolean existsByClassroomAndStartTime(Classroom classroom, LocalDateTime startTime);

    // Lấy danh sách lịch giao nhau với khoảng thời gian truyền vào.
    @Query("SELECT t FROM Timetable t WHERE t.startTime < :end AND t.endTime > :start " +
            "ORDER BY t.startTime ASC")
    List<Timetable> findByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<Timetable> findByClassroom_ClassID(Integer classId);

    void deleteByClassroom_ClassID(Integer classId);

    // Các hàm phục vụ thống kê hôm nay
    long countByStartTimeBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
    long countByStatusAndStartTimeBetween(TimetableStatus status, LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query("SELECT t FROM Timetable t WHERE t.teacher.teacherID = :teacherID " +
            "AND t.startTime >= :start AND t.endTime <= :end ORDER BY t.startTime ASC")
    List<Timetable> findByTeacherAndTimeRange(
            @Param("teacherID") Integer teacherID,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Timetable t " +
            "JOIN t.classroom c " +
            "JOIN c.classMembers cm " +
            "WHERE cm.student.user.username = :username " +
            "AND t.startTime >= :start AND t.endTime <= :end")
    List<Timetable> findTimetablesByStudentUsernameAndTimeRange(
            @Param("username") String username,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
