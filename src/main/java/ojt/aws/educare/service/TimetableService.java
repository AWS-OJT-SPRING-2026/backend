package ojt.aws.educare.service;

import ojt.aws.educare.dto.request.TimetableBulkUpdateRequest;
import ojt.aws.educare.dto.request.TimetableRecurringRequest;
import ojt.aws.educare.dto.request.TimetableRequest;
import ojt.aws.educare.dto.request.UpdateMeetLinkRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.dto.response.TeacherScheduleStatsResponse;
import ojt.aws.educare.dto.response.TimetableResponse;
import ojt.aws.educare.dto.response.TimetableStatsResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface TimetableService {
    ApiResponse<TimetableResponse> createSingleTimetable(TimetableRequest request);
    ApiResponse<Void> createRecurringTimetable(TimetableRecurringRequest request);
    ApiResponse<Void> bulkUpdateTimetable(Integer classID, TimetableBulkUpdateRequest request);
    ApiResponse<TimetableResponse> updateSingleTimetable(Integer iD, TimetableRequest request);
    ApiResponse<List<TimetableResponse>> getTimetables(LocalDateTime start, LocalDateTime end);
    ApiResponse<TimetableStatsResponse> getStats();
    ApiResponse<Void> deleteTimetable(Integer iD);
    ApiResponse<Void> deleteAllByClass(Integer classID);

    ApiResponse<List<TimetableResponse>> getMyScheduleList(LocalDateTime start, LocalDateTime end);
    ApiResponse<TimetableResponse> updateMeetLink(Integer timetableID, UpdateMeetLinkRequest request);
    ApiResponse<TeacherScheduleStatsResponse> getMyScheduleStats(LocalDateTime start, LocalDateTime end);;
}
