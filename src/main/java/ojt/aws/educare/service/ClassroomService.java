package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.ClassroomCreateRequest;
import ojt.aws.educare.dto.request.ClassroomUpdateRequest;
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
}