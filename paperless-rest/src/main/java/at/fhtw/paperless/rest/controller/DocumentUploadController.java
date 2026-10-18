package at.fhtw.paperless.rest.controller;

import at.fhtw.paperless.rest.dto.*;
import at.fhtw.paperless.rest.exception.InvalidDocumentException;
import at.fhtw.paperless.rest.service.DocumentUploadService;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentUploadController {
    private final DocumentUploadService service;
    public DocumentUploadController(DocumentUploadService service) { this.service = service; }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam @NotBlank @Size(max = 255) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @Positive Long categoryId,
            @RequestParam(required = false) @Size(max = 32) List<@NotBlank @Size(max = 50) String> labels) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank() || filename.length() > 255) {
            throw new InvalidDocumentException("Filename must contain 1 to 255 characters");
        }
        var response = service.upload(
                new DocumentCreateRequest(title, filename, description, categoryId, labels), file);
        return ResponseEntity.created(URI.create("/api/documents/" + response.id())).body(response);
    }
}
