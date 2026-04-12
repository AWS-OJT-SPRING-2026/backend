package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.QuizDraftResponse;
import ojt.aws.educare.entity.QuizDraft;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuizDraftMapper {

    @Mapping(target = "questionId", source = "questionId")
    @Mapping(target = "answerRefId", source = "answerRefId")
    QuizDraftResponse.SavedAnswer toSavedAnswer(Integer questionId, Integer answerRefId);

    @Mapping(target = "assignmentId", source = "draft.submission.assignment.assignmentID")
    @Mapping(target = "submissionId", source = "draft.submission.submissionID")
    @Mapping(target = "answers", source = "answers")
    @Mapping(target = "currentQuestion", source = "draft.currentQuestion")
    @Mapping(target = "lastSavedAt", source = "draft.lastSavedAt")
    QuizDraftResponse toResponse(QuizDraft draft, List<QuizDraftResponse.SavedAnswer> answers);
}

