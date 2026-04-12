package ojt.aws.educare.mapper;

import ojt.aws.educare.entity.Book;
import ojt.aws.educare.entity.Classroom;
import ojt.aws.educare.entity.ClassroomMaterial;
import ojt.aws.educare.entity.MaterialType;
import ojt.aws.educare.entity.QuestionBank;
import ojt.aws.educare.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClassroomMaterialMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "classroom", source = "classroom")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "assignedBy", source = "assignedBy")
    @Mapping(target = "book", source = "book")
    @Mapping(target = "questionBank", ignore = true)
    ClassroomMaterial toTheoryMaterial(Classroom classroom, MaterialType type, User assignedBy, Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedAt", ignore = true)
    @Mapping(target = "classroom", source = "classroom")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "assignedBy", source = "assignedBy")
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "questionBank", source = "questionBank")
    ClassroomMaterial toQuestionMaterial(
            Classroom classroom, MaterialType type, User assignedBy, QuestionBank questionBank);
}

