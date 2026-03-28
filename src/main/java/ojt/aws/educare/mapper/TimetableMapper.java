package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.TimetableRequest;
import ojt.aws.educare.dto.response.StudentScheduleResponse;
import ojt.aws.educare.dto.response.TimetableResponse;
import ojt.aws.educare.entity.ClassMember;
import ojt.aws.educare.entity.Timetable;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TimetableMapper {

    @Mapping(target = "timetableID", ignore = true)
    @Mapping(target = "classroom", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", constant = "SCHEDULED")
    Timetable toTimetable(TimetableRequest request);

    @Mapping(target = "classID", source = "classroom.classID")
    @Mapping(target = "className", source = "classroom.className")
    @Mapping(target = "subjectName", source = "classroom.subject.subjectName")
    @Mapping(target = "teacherID", source = "teacher.teacherID")
    @Mapping(target = "teacherName", source = "teacher.fullName")
    TimetableResponse toResponse(Timetable timetable);

    List<TimetableResponse> toResponseList(List<Timetable> timetables);

    @Mapping(target = "timetableID", ignore = true)
    @Mapping(target = "classroom", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateTimetableFromRequest(@MappingTarget Timetable timetable, TimetableRequest request);

    //student
    @Mapping(target = "classID", source = "classroom.classID")
    @Mapping(target = "subjectName", source = "classroom.subject.subjectName")
    @Mapping(target = "className", source = "classroom.className")
    @Mapping(target = "teacherName", source = "teacher.fullName", defaultValue = "Chưa phân công")
    @Mapping(target = "meetUrl", source = "googleMeetLink")
    @Mapping(target = "studentCount", expression = "java(timetable.getClassroom().getClassMembers() != null ? timetable.getClassroom().getClassMembers().size() : 0)")
    @Mapping(target = "classmates", source = "classroom.classMembers")
    @Mapping(target = "attendanceStatus", expression = "java(attendanceMap != null ? attendanceMap.get(timetable.getTimetableID()) : null)")
    StudentScheduleResponse toStudentScheduleResponse(Timetable timetable, @Context Map<Integer, String> attendanceMap);

    List<StudentScheduleResponse> toStudentScheduleResponseList(List<Timetable> timetables, @Context Map<Integer, String> attendanceMap);

    @Mapping(target = "studentID", source = "student.studentID")
    @Mapping(target = "fullName", source = "student.fullName")
    @Mapping(target = "avatarUrl", source = "student.user.avatarUrl")
    StudentScheduleResponse.ClassmateResponse toClassmateResponse(ClassMember classMember);
}