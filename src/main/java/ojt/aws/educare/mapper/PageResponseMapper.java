package ojt.aws.educare.mapper;

import ojt.aws.educare.dto.response.PageResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PageResponseMapper {

    public <T> PageResponse<T> toPageResponse(
            int currentPage,
            int pageSize,
            int totalPages,
            long totalElements,
            List<T> data
    ) {
        return PageResponse.<T>builder()
                .currentPage(currentPage)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .data(data)
                .build();
    }
}

