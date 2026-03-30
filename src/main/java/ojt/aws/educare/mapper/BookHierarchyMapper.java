package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.*;
import ojt.aws.educare.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookHierarchyMapper {
    @Mapping(target = "subjectId", source = "subject.subjectID")
    @Mapping(target = "subjectName", source = "subject.subjectName")
    BookHierarchyResponse toBookHierarchyResponse(Book book);

    ChapterResponse toChapterResponse(Chapter chapter);
    LessonResponse toLessonResponse(Lesson lesson);
    SectionResponse toSectionResponse(Section section);
    SubsectionResponse toSubsectionResponse(Subsection subsection);
    ContentBlockResponse toContentBlockResponse(ContentBlock contentBlock);
}
