package at.fhtw.paperless.rest.mapper;

import at.fhtw.paperless.rest.dto.DocumentResponse;
import at.fhtw.paperless.rest.entity.Category;
import at.fhtw.paperless.rest.entity.Document;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DocumentMapperTest {
    private final DocumentMapper mapper = new DocumentMapper();

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
        assertEquals(new DocumentResponse(1L, "Invoice", "invoice.pdf", "Details",
                created, updated, 2L, "Finance"), mapper.toResponse(document));
    }

    @Test
    void mapsDocumentWithoutCategory() {
        var response = mapper.toResponse(new Document());
        assertNull(response.categoryId());
        assertNull(response.categoryName());
    }
}
