package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.ClassroomCreateRequest;
import ojt.aws.educare.dto.request.ClassroomUpdateRequest;
import ojt.aws.educare.dto.response.ClassroomDetailResponse;
import ojt.aws.educare.dto.response.ClassroomResponse;
import ojt.aws.educare.entity.ClassMember;
import ojt.aws.educare.entity.Classroom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClassroomMapper {

    @Mapping(target = "classID", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "classMembers", ignore = true)
    @Mapping(target = "materials", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "timetables", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Classroom toClassroom(ClassroomCreateRequest request);

    @Mapping(target = "subjectName", source = "subject.subjectName")
    @Mapping(target = "teacherName", source = "teacher.fullName")
    @Mapping(target = "currentStudents", expression = "java(classroom.getClassMembers() != null ? classroom.getClassMembers().size() : 0)")
    ClassroomResponse toClassroomResponse(Classroom classroom);

    List<ClassroomResponse> toClassroomResponseList(List<Classroom> classrooms);

    @Mapping(target = "subjectID", source = "subject.subjectID")
    @Mapping(target = "subjectName", source = "subject.subjectName")
    @Mapping(target = "teacherID", source = "teacher.teacherID")
    @Mapping(target = "teacherName", source = "teacher.fullName")
    @Mapping(target = "currentStudents", expression = "java(classroom.getClassMembers() != null ? classroom.getClassMembers().size() : 0)")
    @Mapping(target = "students", source = "classMembers") // Map list ClassMember -> List StudentInClassResponse
    ClassroomDetailResponse toClassroomDetailResponse(Classroom classroom);

    @Mapping(target = "studentID", source = "student.studentID")
    @Mapping(target = "fullName", source = "student.fullName")
    @Mapping(target = "gender", source = "student.gender")
    @Mapping(target = "email", source = "student.user.email")
    @Mapping(target = "phone", source = "student.user.phone")
    @Mapping(target = "memberStatus", source = "status")
    ClassroomDetailResponse.StudentInClassResponse toStudentInClassResponse(ClassMember classMember);

    @Mapping(target = "classID", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "classMembers", ignore = true)
    @Mapping(target = "materials", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "timetables", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateClassroomFromRequest(@MappingTarget Classroom classroom, ClassroomUpdateRequest request);

}