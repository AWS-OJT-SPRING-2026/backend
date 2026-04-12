package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.*;
import ojt.aws.educare.dto.response.StudentResponse;
import ojt.aws.educare.dto.response.TeacherResponse;
import ojt.aws.educare.dto.response.UserResponse;
import ojt.aws.educare.entity.PasswordResetToken;
import ojt.aws.educare.entity.Classroom;
import ojt.aws.educare.entity.Student;
import ojt.aws.educare.entity.Teacher;
import ojt.aws.educare.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserRegisterRequest request);

    User toUser(TeacherCreateRequest request);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "teacherID", ignore = true)
    @Mapping(target = "classrooms", ignore = true)
    Teacher toTeacher(TeacherCreateRequest request);

    User toUser(StudentCreateRequest request);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "studentID", ignore = true)
    @Mapping(target = "classMembers", ignore = true)
    @Mapping(target = "aiChatSessions", ignore = true)
    @Mapping(target = "roadmaps", ignore = true)
    @Mapping(target = "learningProfiles", ignore = true)
    Student toStudent(StudentCreateRequest request);

    @Mapping(target = "role", source = "role")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "classes", source = "user", qualifiedByName = "mapUserToClassNames")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    UserResponse toUserResponse(User user);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    StudentResponse toStudentResponse(Student student);

    List<StudentResponse> toStudentResponseList(List<Student> students);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "classes", source = "user", qualifiedByName = "mapUserToClassNames")
    @Mapping(target = "avatarUrl", source = "user.avatarUrl")
    TeacherResponse toTeacherResponse(Teacher teacher);

    List<TeacherResponse> toTeacherResponseList(List<Teacher> teachers);

    List<UserResponse> toUserResponseList(List<User> users);

    @Named("mapUserToClassNames")
    default List<String> mapUserToClassNames(User user) {
        if (user == null)
            return null;

        // Nếu là Teacher
        if (user.getTeacher() != null && user.getTeacher().getClassrooms() != null) {
            return user.getTeacher().getClassrooms().stream()
                    .map(Classroom::getClassName)
                    .collect(Collectors.toList());
        }

        // Nếu là Student
        if (user.getStudent() != null && user.getStudent().getClassMembers() != null) {
            return user.getStudent().getClassMembers().stream()
                    .map(classMember -> classMember.getClassroom().getClassName())
                    .collect(Collectors.toList());
        }

        return null;
    }

    // update
    void updateUserFromTeacherRequest(@MappingTarget User user, TeacherUpdateRequest request);

    void updateUserFromStudentRequest(@MappingTarget User user, StudentUpdateRequest request);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "teacherID", ignore = true)
    @Mapping(target = "classrooms", ignore = true)
    void updateTeacherFromRequest(@MappingTarget Teacher teacher, TeacherUpdateRequest request);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "studentID", ignore = true)
    @Mapping(target = "classMembers", ignore = true)
    @Mapping(target = "aiChatSessions", ignore = true)
    @Mapping(target = "roadmaps", ignore = true)
    @Mapping(target = "learningProfiles", ignore = true)
    void updateStudentFromRequest(@MappingTarget Student student, StudentUpdateRequest request);

    default PasswordResetToken toPasswordResetToken(
            String token,
            String otpCode,
            LocalDateTime expiryDate,
            Boolean used,
            User user,
            LocalDateTime createdDate
    ) {
        return PasswordResetToken.builder()
                .token(token)
                .otpCode(otpCode)
                .expiryDate(expiryDate)
                .used(used)
                .user(user)
                .createdDate(createdDate)
                .build();
    }
}
