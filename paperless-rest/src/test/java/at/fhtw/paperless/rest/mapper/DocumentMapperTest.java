package at.fhtw.paperless.rest.mapper;

import at.fhtw.paperless.rest.dto.DocumentResponse;
import at.fhtw.paperless.rest.entity.Category;
import at.fhtw.paperless.rest.entity.Document;
import at.fhtw.paperless.rest.entity.Label;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DocumentMapperTest {
    private final DocumentMapper mapper = createMapper();

    private static DocumentMapper createMapper() {
        var mapper = org.mapstruct.factory.Mappers.getMapper(DocumentMapper.class);
        try {
            var field = at.fhtw.paperless.rest.mapper.DocumentMapperImpl.class.getDeclaredField("labelMapper");
            field.setAccessible(true);
            field.set(mapper, org.mapstruct.factory.Mappers.getMapper(LabelMapper.class));
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Could not wire nested LabelMapper for tests", failure);
        }
        return mapper;
    }

    @Test
    void mapsAllFieldsWithCategory() {
        Category category = new Category();
        category.setId(2L);
        category.setName("Finance");
        Document document = new Document();
        document.setId(1L);
        document.setTitle("Invoice");
        document.setFilename("invoice.pdf");
        document.setDescription("Details");
        LocalDateTime created = LocalDateTime.of(2026, 9, 1, 10, 0);
        LocalDateTime updated = created.plusDays(1);
        document.setCreatedAt(created);
        document.setUpdatedAt(updated);
        document.setCategory(category);
        Label label = new Label();
        label.setId(7L);
        label.setName("urgent");
        label.setColor("#B91C1C");
        document.setLabels(List.of(label));
        assertEquals(new DocumentResponse(1L, "Invoice", "invoice.pdf", "Details",
                created, updated, 2L, "Finance",
                List.of(new at.fhtw.paperless.rest.dto.LabelResponse(7L, "urgent", "#B91C1C"))),
                mapper.toResponse(document));
    }

    @Test
    void mapsDocumentWithoutCategory() {
        var response = mapper.toResponse(new Document());
        assertNull(response.categoryId());
        assertNull(response.categoryName());
    }
}
