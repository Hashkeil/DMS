package at.fhtw.paperless.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

import java.util.List;

// labels == null keeps the document's existing tags; a given list replaces them.
public record DocumentUpdateRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 255) String filename,
        String description,
        @Positive Long categoryId,
        @Size(max = 32) List<@NotBlank @Size(max = 50) String> labels
) {
}
