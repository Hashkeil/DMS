package at.fhtw.paperless.rest.mapper;
import at.fhtw.paperless.rest.dto.DocumentResponse;
import at.fhtw.paperless.rest.dto.LabelResponse;
import at.fhtw.paperless.rest.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = LabelMapper.class, componentModel = "spring")
public interface DocumentMapper {
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    DocumentResponse toResponse(Document document);
}
