package at.fhtw.paperless.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LabelCreateRequest(
        @NotBlank @Size(max = 50) String name,
        @Pattern(regexp = "#[0-9A-Fa-f]{6}", message = "color must be a hex value like #4F46E5") String color
) {
}
