package at.fhtw.paperless.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DocumentResponse(
        Long id,
        String title,
        String filename,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long categoryId,
        String categoryName,
        List<LabelResponse> labels
) {
}
