package at.fhtw.paperless.rest.service;

import at.fhtw.paperless.rest.dto.LabelCreateRequest;
import at.fhtw.paperless.rest.dto.LabelResponse;
import at.fhtw.paperless.rest.entity.Document;
import at.fhtw.paperless.rest.entity.Label;
import at.fhtw.paperless.rest.exception.LabelNotFoundException;
import at.fhtw.paperless.rest.mapper.LabelMapper;
import at.fhtw.paperless.rest.repository.DocumentRepository;
import at.fhtw.paperless.rest.repository.LabelRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Tagging (Document <-- m:n --> Label): labels are auto-created on first use,
// so tagging a document never fails because a tag is "missing" yet.
@Service
@Validated
@Transactional(readOnly = true)
public class LabelService {
    // Palette assigned deterministically per label name so tags look consistent.
    private static final String[] PALETTE = {
            "#4F46E5", "#0F766E", "#B45309", "#BE185D", "#7C3AED",
            "#15803D", "#B91C1C", "#0369A1", "#A16207", "#0E7490"
    };

    private final LabelRepository labels;
    private final LabelMapper mapper;
    private final DocumentRepository documents;

    public LabelService(LabelRepository labels, LabelMapper mapper, DocumentRepository documents) {
        this.labels = labels;
        this.mapper = mapper;
        this.documents = documents;
    }

    public List<LabelResponse> getAll() {
        return labels.findAll().stream().map(mapper::toResponse).sorted(Comparator
                .comparing(LabelResponse::name, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public boolean existsByName(String name) {
        return labels.existsByNameIgnoreCase(name == null ? "" : name.trim());
    }

    @Transactional
    public LabelResponse create(@NotNull @Valid LabelCreateRequest request) {
        String name = request.name().trim();
        return labels.findByNameIgnoreCase(name)
                .map(mapper::toResponse)
                .orElseGet(() -> {
                    Label label = new Label();
                    label.setName(name);
                    label.setColor(request.color() != null ? request.color() : colorFor(name));
                    return mapper.toResponse(labels.saveAndFlush(label));
                });
    }

    @Transactional
    public void delete(long id) {
        Label label = labels.findById(id).orElseThrow(() -> new LabelNotFoundException(id));
        // ManyToMany join rows are owned by Document; clear references first.
        documents.deleteLabelReference(id);
        labels.delete(label);
    }

    // Resolves tag names to managed Label entities, creating missing ones.
    @Transactional
    public List<Label> resolve(List<String> tagNames) {
        if (tagNames == null) {
            return List.of();
        }
        Set<String> uniqueNames = new LinkedHashSet<>();
        for (String name : tagNames) {
            String trimmed = name == null ? "" : name.trim();
            if (!trimmed.isEmpty()) {
                uniqueNames.add(trimmed);
            }
        }
        return uniqueNames.stream().map(name -> labels.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Label label = new Label();
                    label.setName(name);
                    label.setColor(colorFor(name));
                    return label;
                })).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }

    private String colorFor(String name) {
        int hash = Math.abs(name.toLowerCase(Locale.ROOT).hashCode());
        return PALETTE[hash % PALETTE.length];
    }
}
