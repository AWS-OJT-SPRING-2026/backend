package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.AttendanceRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.AttendanceStudentResponse;

import java.util.List;

public interface AttendanceService {
    ApiResponse<Void> saveAttendance(Integer timetableID, List<AttendanceRequest> requests);
    ApiResponse<List<AttendanceStudentResponse>> getAttendanceByTimetable(Integer timetableId);
}
