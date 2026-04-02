package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.ClassroomCreateRequest;
import ojt.aws.educare.dto.request.ClassroomUpdateRequest;
import ojt.aws.educare.dto.request.ExportReportRequest;
import ojt.aws.educare.dto.response.*;
import ojt.aws.educare.dto.response.TeacherClassroomOptionResponse;

import java.util.List;

public interface ClassroomService {
    ApiResponse<PageResponse<ClassroomResponse>> getAllClassrooms(int page, int size);
    ApiResponse<ClassroomStatsResponse> getClassroomStats();
    ApiResponse<ClassroomResponse> createClassroom(ClassroomCreateRequest request);
    ApiResponse<Void> assignTeacher(Integer classID, Integer teacherID);
    ApiResponse<Void> addStudentsToClass(Integer classID, List<Integer> studentIDs);
    ApiResponse<Void> removeStudentFromClass(Integer classID, Integer studentID);
    ApiResponse<Void> toggleStudentStatusInClass(Integer classID, Integer studentID);
    ApiResponse<ClassroomDetailResponse> getClassroomByID(Integer classID);
    ApiResponse<ClassroomResponse> updateClassroom(Integer classID, ClassroomUpdateRequest request);
    ApiResponse<Void> toggleClassroomStatus(Integer classID);
    ApiResponse<List<TeacherClassroomOptionResponse>> getMyClassroomOptions();

    /** Filter the student roster of a class by keyword (name / MSSV) and/or status. */
    ApiResponse<List<ClassroomDetailResponse.StudentInClassResponse>> getStudentsByClass(
            Integer classID, String keyword, String status);

    ApiResponse<ClassDashboardResponse> getClassDashboard(Integer classID);

    ApiResponse<PageResponse<ClassStudentResponse>> getStudentsByClassPaged(Integer classID, String keyword, String status, int page, int size);

    ApiResponse<PageResponse<ClassNotificationResponse>> getClassNotifications(Integer classID, String category, int page, int size);

    ApiResponse<WeeklyGradeStatisticsResponse> getWeeklyGradeStatistics(Integer classID);

    ApiResponse<ExportReportResponse> exportClassReport(Integer classID, ExportReportRequest request);

    ExportFilePayload downloadClassReport(Integer classID, ExportReportRequest request);
}