package at.fhtw.paperless.rest.mapper;

import at.fhtw.paperless.rest.dto.DocumentResponse;
import at.fhtw.paperless.rest.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    public DocumentResponse toResponse(Document document) {
        var category = document.getCategory();
        return new DocumentResponse(document.getId(), document.getTitle(),
                document.getFilename(), document.getDescription(),
                document.getCreatedAt(), document.getUpdatedAt(),
                category == null ? null : category.getId(),
                category == null ? null : category.getName());
    }
}
