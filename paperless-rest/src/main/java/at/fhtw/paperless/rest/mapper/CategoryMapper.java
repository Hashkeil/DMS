package at.fhtw.paperless.rest.mapper;
import at.fhtw.paperless.rest.dto.CategoryResponse;
import at.fhtw.paperless.rest.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}
