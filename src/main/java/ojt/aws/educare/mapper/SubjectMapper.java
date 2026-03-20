package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.SubjectResponse;
import ojt.aws.educare.entity.Subject;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubjectMapper {
    SubjectResponse toSubjectResponse(Subject subject);
    List<SubjectResponse> toSubjectResponseList(List<Subject> subjects);
}