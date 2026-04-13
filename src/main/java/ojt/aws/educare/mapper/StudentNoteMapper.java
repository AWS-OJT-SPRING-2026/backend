package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.request.StudentNoteRequest;
import ojt.aws.educare.dto.response.StudentNoteResponse;
import ojt.aws.educare.entity.StudentNote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentNoteMapper {

    @Mapping(target = "noteID", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "noteDate", source = "date")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StudentNote toStudentNote(StudentNoteRequest request);

    @Mapping(target = "date", source = "noteDate")
    StudentNoteResponse toResponse(StudentNote note);
}

