package ojt.aws.educare.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.request.DocumentDistributionUpdateRequest;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.service.DocumentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DocumentController {

    DocumentService documentService;

    @PutMapping("/{id}/distributions")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<Void> updateDistributions(
            @PathVariable Integer id,
            @RequestBody DocumentDistributionUpdateRequest request
    ) {
        return documentService.updateDistributions(id, request);
    }
}

