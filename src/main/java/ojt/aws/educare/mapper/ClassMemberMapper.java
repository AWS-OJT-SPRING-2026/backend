package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.ClassroomCreateRequest;
import ojt.aws.educare.entity.ClassMember;
import ojt.aws.educare.entity.ClassMemberID;
import ojt.aws.educare.entity.Classroom;
import ojt.aws.educare.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClassMemberMapper {

    @Mapping(target = "classMemberID", source = "memberID")
    @Mapping(target = "classroom", source = "classroom")
    @Mapping(target = "student", source = "student")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "joinedAt", ignore = true)
    ClassMember toClassMember(ClassMemberID memberID, Classroom classroom, Student student);
}
