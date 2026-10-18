package at.fhtw.paperless.rest.controller;

import at.fhtw.paperless.rest.dto.LabelCreateRequest;
import at.fhtw.paperless.rest.dto.LabelResponse;
import at.fhtw.paperless.rest.service.LabelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/labels")
public class LabelController {
    private final LabelService service;

    public LabelController(LabelService service) {
        this.service = service;
    }

    @GetMapping
    public List<LabelResponse> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<LabelResponse> create(@Valid @RequestBody LabelCreateRequest request) {
        // Idempotent: re-creating an existing tag returns it instead of erroring.
        boolean existing = service.existsByName(request.name());
        LabelResponse response = service.create(request);
        return existing ? ResponseEntity.ok(response)
                : ResponseEntity.created(URI.create("/api/labels/" + response.id())).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
