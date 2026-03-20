package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.AttendanceRequest;
import ojt.aws.educare.dto.response.AttendanceStudentResponse;
import ojt.aws.educare.entity.Attendance;
import ojt.aws.educare.entity.Student;
import ojt.aws.educare.entity.Timetable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {

    @Mapping(target = "attendanceID", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "timetable", source = "timetable")
    @Mapping(target = "student", source = "student")
    @Mapping(target = "status", source = "request.status")
    @Mapping(target = "note", source = "request.note")
    Attendance toAttendance(AttendanceRequest request, Timetable timetable, Student student);

    @Mapping(target = "attendanceID", ignore = true)
    @Mapping(target = "timetable", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateAttendanceFromRequest(@MappingTarget Attendance attendance, AttendanceRequest request);

    @Mapping(target = "studentID", source = "student.studentID")
    @Mapping(target = "studentCode", expression = "java(\"HS-\" + String.format(\"%04d\", student.getStudentID()))")
    @Mapping(target = "fullName", source = "student.fullName")
    @Mapping(target = "status", expression = "java(attendance != null && attendance.getStatus() != null ? attendance.getStatus() : \"PRESENT\")")
    @Mapping(target = "note", expression = "java(attendance != null && attendance.getNote() != null ? attendance.getNote() : \"\")")
    AttendanceStudentResponse toAttendanceStudentResponse(Student student, Attendance attendance);
}