package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.TimetableRequest;
import ojt.aws.educare.dto.response.TimetableResponse;
import ojt.aws.educare.entity.Timetable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

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
}