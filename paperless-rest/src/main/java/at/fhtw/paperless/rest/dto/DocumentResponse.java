package at.fhtw.paperless.rest.dto;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String title,
        String filename,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long categoryId,
        String categoryName
) {
}
