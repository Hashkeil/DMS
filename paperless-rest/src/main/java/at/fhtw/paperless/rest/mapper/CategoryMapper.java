package at.fhtw.paperless.rest.mapper;

import at.fhtw.paperless.rest.dto.CategoryResponse;
import at.fhtw.paperless.rest.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
