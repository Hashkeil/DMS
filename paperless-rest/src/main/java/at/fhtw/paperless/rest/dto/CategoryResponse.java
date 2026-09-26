package at.fhtw.paperless.rest.dto;

public record CategoryResponse(
        Long id,
        String name,
        String description
) {
}
