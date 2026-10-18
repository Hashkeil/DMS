package at.fhtw.paperless.rest.dto;

public record LabelResponse(
        Long id,
        String name,
        String color
) {
}
