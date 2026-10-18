package at.fhtw.paperless.rest.service;

import at.fhtw.paperless.rest.dto.*;
import at.fhtw.paperless.rest.entity.Category;
import at.fhtw.paperless.rest.entity.Document;
import at.fhtw.paperless.rest.exception.CategoryNotFoundException;
import at.fhtw.paperless.rest.exception.DocumentNotFoundException;
import at.fhtw.paperless.rest.mapper.DocumentMapper;
import at.fhtw.paperless.rest.repository.CategoryRepository;
import at.fhtw.paperless.rest.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@org.springframework.validation.annotation.Validated
@Transactional(readOnly = true)
public class DocumentService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DocumentService.class);
    private final DocumentRepository documentRepository;
    private final CategoryRepository categoryRepository;
    private final LabelService labelService;
    private final DocumentMapper mapper;

    public DocumentService(DocumentRepository documentRepository, CategoryRepository categoryRepository,
                           LabelService labelService, DocumentMapper mapper) {
        this.documentRepository = documentRepository;
        this.categoryRepository = categoryRepository;
        this.labelService = labelService;
        this.mapper = mapper;
    }

    @Transactional
    public DocumentResponse create(@jakarta.validation.Valid @jakarta.validation.constraints.NotNull DocumentCreateRequest request) {
        log.info("Creating document metadata");
        Category category = findCategory(request.categoryId());
        Document document = new Document();
        document.setTitle(request.title());
        document.setFilename(request.filename());
        document.setDescription(request.description());
        document.setCategory(category);
        document.setLabels(labelService.resolve(request.labels()));
        return mapper.toResponse(documentRepository.saveAndFlush(document));
    }

    public List<DocumentResponse> getAll() {
        return documentRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    public DocumentResponse getById(Long id) {
        return mapper.toResponse(findDocument(id));
    }

    @Transactional
    public DocumentResponse update(Long id, @jakarta.validation.Valid @jakarta.validation.constraints.NotNull DocumentUpdateRequest request) {
        log.info("Updating document: id={}", id);
        Document document = findDocument(id);
        Category category = findCategory(request.categoryId());
        document.setTitle(request.title());
        document.setFilename(request.filename());
        document.setDescription(request.description());
        document.setCategory(category);
        if (request.labels() != null) {
            document.setLabels(labelService.resolve(request.labels()));
        }
        return mapper.toResponse(documentRepository.saveAndFlush(document));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting document: id={}", id);
        documentRepository.delete(findDocument(id));
    }

    private Document findDocument(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new DocumentNotFoundException(id));
    }

    private Category findCategory(Long id) {
        return id == null ? null : categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
