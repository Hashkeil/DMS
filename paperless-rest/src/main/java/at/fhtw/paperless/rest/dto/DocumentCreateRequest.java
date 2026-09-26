package at.fhtw.paperless.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

public record DocumentCreateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String filename,
        String description,
        @Positive Long categoryId
) {
}
