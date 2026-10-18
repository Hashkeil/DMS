package at.fhtw.paperless.rest.mapper;

import at.fhtw.paperless.rest.dto.LabelResponse;
import at.fhtw.paperless.rest.entity.Label;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LabelMapper {
    LabelResponse toResponse(Label label);
}
