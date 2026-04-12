package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.ClassroomCreateRequest;
import ojt.aws.educare.dto.request.ClassroomUpdateRequest;
import ojt.aws.educare.dto.request.ExportReportRequest;
import ojt.aws.educare.dto.response.ClassDashboardResponse;
import ojt.aws.educare.dto.response.ClassStudentResponse;
import ojt.aws.educare.dto.response.ClassroomDetailResponse;
import ojt.aws.educare.dto.response.ClassroomResponse;
import ojt.aws.educare.dto.response.ClassroomStatsResponse;
import ojt.aws.educare.dto.response.ClassNotificationResponse;
import ojt.aws.educare.dto.response.ExportFilePayload;
import ojt.aws.educare.dto.response.ExportReportResponse;
import ojt.aws.educare.dto.response.TeacherClassroomOptionResponse;
import ojt.aws.educare.dto.response.WeeklyGradeDayResponse;
import ojt.aws.educare.dto.response.WeeklyGradeStatisticsResponse;
import ojt.aws.educare.entity.ClassMember;
import ojt.aws.educare.entity.Classroom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ClassroomMapper {

    @Mapping(target = "classID", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "classMembers", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "timetables", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    Classroom toClassroom(ClassroomCreateRequest request);

    @Mapping(target = "subjectName", source = "subject.subjectName")
    @Mapping(target = "teacherName", source = "teacher.fullName")
    @Mapping(target = "currentStudents", expression = "java(classroom.getClassMembers() != null ? classroom.getClassMembers().size() : 0)")
    ClassroomResponse toClassroomResponse(Classroom classroom);

    List<ClassroomResponse> toClassroomResponseList(List<Classroom> classrooms);

    @Mapping(target = "subjectID", source = "subject.subjectID")
    @Mapping(target = "subjectName", source = "subject.subjectName")
    TeacherClassroomOptionResponse toTeacherClassroomOptionResponse(Classroom classroom);

    List<TeacherClassroomOptionResponse> toTeacherClassroomOptionResponseList(List<Classroom> classrooms);

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
    @Mapping(target = "dateOfBirth", source = "student.dateOfBirth")
    @Mapping(target = "address", source = "student.address")
    @Mapping(target = "email", source = "student.user.email")
    @Mapping(target = "phone", source = "student.user.phone")
    @Mapping(target = "avatarUrl", source = "student.user.avatarUrl")
    @Mapping(target = "memberStatus", source = "status")
    ClassroomDetailResponse.StudentInClassResponse toStudentInClassResponse(ClassMember classMember);

    @Mapping(target = "classID", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "classMembers", ignore = true)
//    @Mapping(target = "materials", ignore = true)
    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "timetables", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateClassroomFromRequest(@MappingTarget Classroom classroom, ClassroomUpdateRequest request);

    default ClassroomStatsResponse toClassroomStatsResponse(
            long totalClasses,
            long activeClasses,
            long unassignedClasses,
            int averageClassSize
    ) {
        return ClassroomStatsResponse.builder()
                .totalClasses(totalClasses)
                .activeClasses(activeClasses)
                .unassignedClasses(unassignedClasses)
                .averageClassSize(averageClassSize)
                .build();
    }

    default ClassDashboardResponse toClassDashboardResponse(
            Integer classID,
            String className,
            Integer totalStudents,
            Integer onlineStudents,
            Integer offlineStudents,
            Integer attentionStudents,
            Double averageGpa
    ) {
        return ClassDashboardResponse.builder()
                .classID(classID)
                .className(className)
                .totalStudents(totalStudents)
                .onlineStudents(onlineStudents)
                .offlineStudents(offlineStudents)
                .attentionStudents(attentionStudents)
                .averageGpa(averageGpa)
                .build();
    }

    default ClassNotificationResponse toClassNotificationResponse(
            Long id,
            String category,
            String title,
            String body,
            LocalDateTime createdAt
    ) {
        return ClassNotificationResponse.builder()
                .id(id)
                .category(category)
                .title(title)
                .body(body)
                .createdAt(createdAt)
                .build();
    }

    default WeeklyGradeStatisticsResponse toWeeklyGradeStatisticsResponse(
            Integer classID,
            Integer classCapacity,
            List<WeeklyGradeDayResponse> days
    ) {
        return WeeklyGradeStatisticsResponse.builder()
                .classID(classID)
                .classCapacity(classCapacity)
                .days(days)
                .build();
    }

    default ExportReportResponse toExportReportResponse(ExportFilePayload payload, ExportReportRequest request) {
        return ExportReportResponse.builder()
                .fileName(payload.getFileName())
                .format(request.getFormat().toUpperCase())
                .dataTypes(request.getDataTypes().stream().map(String::toUpperCase).toList())
                .timeRange(request.getTimeRange().toUpperCase())
                .status("READY")
                .message("Đã tạo file báo cáo thành công.")
                .downloadUrl(null)
                .build();
    }

    default ExportFilePayload toExportFilePayload(String fileName, String contentType, byte[] content) {
        return ExportFilePayload.builder()
                .fileName(fileName)
                .contentType(contentType)
                .content(content)
                .build();
    }

    default ClassStudentResponse toClassStudentResponse(
            Integer studentId,
            String fullName,
            String mssv,
            String avatarUrl,
            Double completionRate,
            Double gpa,
            Integer missingCount,
            LocalDateTime lastActiveTime,
            String status
    ) {
        return ClassStudentResponse.builder()
                .studentId(studentId)
                .fullName(fullName)
                .mssv(mssv)
                .avatarUrl(avatarUrl)
                .completionRate(completionRate)
                .gpa(gpa)
                .missingCount(missingCount)
                .lastActiveTime(lastActiveTime)
                .status(status)
                .build();
    }

}