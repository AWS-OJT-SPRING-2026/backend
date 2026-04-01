package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.AnswerResponse;
import ojt.aws.educare.dto.response.QuestionPreviewResponse;
import ojt.aws.educare.entity.Question;
import ojt.aws.educare.repository.QuestionRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    @Mapping(target = "difficultyLabel", source = "question.difficultyLevel", qualifiedByName = "mapDifficultyLabel")
    @Mapping(target = "bankId", source = "question.bank.id")
    @Mapping(target = "bankName", source = "question.bank.bankName")
    QuestionPreviewResponse toPreviewResponse(Question question, List<AnswerResponse> answers);

    @Mapping(target = "difficultyLabel", source = "question.difficultyLevel", qualifiedByName = "mapDifficultyLabel")
    QuestionPreviewResponse toPreviewResponse(
            QuestionRepository.QuestionRandomProjection question,
            List<AnswerResponse> answers
    );

    @Named("mapDifficultyLabel")
    default String mapDifficultyLabel(Integer level) {
        if (level == null) return "Không xác định";
        return switch (level) {
            case 1 -> "Dễ";
            case 2 -> "Trung bình";
            case 3 -> "Khó";
            default -> "Không xác định";
        };
    }
}
