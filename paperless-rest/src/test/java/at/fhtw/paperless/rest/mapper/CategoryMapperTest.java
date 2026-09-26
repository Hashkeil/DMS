package at.fhtw.paperless.rest.mapper;

import at.fhtw.paperless.rest.dto.CategoryResponse;
import at.fhtw.paperless.rest.entity.Category;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {
    @Test
    void mapsAllFields() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Finance");
        category.setDescription("Invoices");
        assertEquals(new CategoryResponse(1L, "Finance", "Invoices"), new CategoryMapper().toResponse(category));
    }
}
