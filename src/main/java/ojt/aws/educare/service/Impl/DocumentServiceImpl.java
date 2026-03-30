package ojt.aws.educare.service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.DocumentDistributionUpdateRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.entity.*;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.repository.*;
import ojt.aws.educare.service.DocumentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentServiceImpl implements DocumentService {
    ClassroomMaterialRepository classroomMaterialRepository;
    ClassroomRepository classroomRepository;
    BookRepository bookRepository;
    QuestionBankRepository questionBankRepository;
    UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<Void> updateDistributions(Integer documentId, DocumentDistributionUpdateRequest request) {
        String normalizedType = normalizeType(request.getType());
        User currentUser = getCurrentUser();

        List<ClassroomMaterial> currentMaterials = getCurrentMaterials(documentId, normalizedType);
        Set<Integer> currentClassIds = currentMaterials.stream()
                .map(cm -> cm.getClassroom().getClassID())
                .collect(Collectors.toSet());

        Set<Integer> newClassIds = request.getClassIds() == null
                ? new HashSet<>()
                : new HashSet<>(request.getClassIds());

        Set<Integer> idsToAdd = new HashSet<>(newClassIds);
        idsToAdd.removeAll(currentClassIds);

        Set<Integer> idsToRemove = new HashSet<>(currentClassIds);
        idsToRemove.removeAll(newClassIds);

        if (!idsToRemove.isEmpty()) {
            List<ClassroomMaterial> toDelete = currentMaterials.stream()
                    .filter(cm -> idsToRemove.contains(cm.getClassroom().getClassID()))
                    .toList();
            classroomMaterialRepository.deleteAll(toDelete);
        }

        if (!idsToAdd.isEmpty()) {
            Map<Integer, Classroom> classroomMap = classroomRepository.findAllById(idsToAdd).stream()
                    .collect(Collectors.toMap(Classroom::getClassID, c -> c));

            if (classroomMap.size() != idsToAdd.size()) {
                throw new AppException(ErrorCode.CLASSROOM_NOT_FOUND);
            }

            List<ClassroomMaterial> toInsert = new ArrayList<>();
            for (Integer classId : idsToAdd) {
                Classroom classroom = classroomMap.get(classId);
                validateTeacherOwnsClassroom(currentUser, classroom);

                ClassroomMaterial material = ClassroomMaterial.builder()
                        .classroom(classroom)
                        .type(normalizedType)
                        .assignedBy(currentUser)
                        .build();

                if ("THEORY".equals(normalizedType)) {
                    Book book = bookRepository.findById(documentId)
                            .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
                    material.setBook(book);
                    material.setQuestionBank(null);
                } else {
                    QuestionBank questionBank = questionBankRepository.findById(documentId)
                            .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
                    material.setQuestionBank(questionBank);
                    material.setBook(null);
                }

                toInsert.add(material);
            }

            classroomMaterialRepository.saveAll(toInsert);
        }

        return ApiResponse.success("Cập nhật phân phối tài liệu thành công", null);
    }

    private List<ClassroomMaterial> getCurrentMaterials(Integer documentId, String normalizedType) {
        if ("THEORY".equals(normalizedType)) {
            if (!bookRepository.existsById(documentId)) {
                throw new AppException(ErrorCode.DOCUMENT_NOT_FOUND);
            }
            return classroomMaterialRepository.findByBook_IdAndType(documentId, normalizedType);
        }

        if (!questionBankRepository.existsById(documentId)) {
            throw new AppException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        return classroomMaterialRepository.findByQuestionBank_IdAndType(documentId, normalizedType);
    }

    private String normalizeType(String type) {
        if (type == null) {
            throw new AppException(ErrorCode.DOCUMENT_TYPE_INVALID);
        }

        String normalized = type.trim().toUpperCase(Locale.ROOT);
        if (!"THEORY".equals(normalized) && !"QUESTION".equals(normalized)) {
            throw new AppException(ErrorCode.DOCUMENT_TYPE_INVALID);
        }

        return normalized;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private void validateTeacherOwnsClassroom(User currentUser, Classroom classroom) {
        if (classroom.getTeacher() == null || classroom.getTeacher().getUser() == null) {
            throw new AppException(ErrorCode.NO_PERMISSION_DISTRIBUTE_DOCUMENT);
        }

        Integer teacherUserId = classroom.getTeacher().getUser().getUserID();
        if (!Objects.equals(teacherUserId, currentUser.getUserID())) {
            throw new AppException(ErrorCode.NO_PERMISSION_DISTRIBUTE_DOCUMENT);
        }
    }
}

