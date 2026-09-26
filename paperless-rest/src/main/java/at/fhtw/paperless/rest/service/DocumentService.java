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
@Transactional(readOnly = true)
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final CategoryRepository categoryRepository;
    private final DocumentMapper mapper;

    public DocumentService(DocumentRepository documentRepository, CategoryRepository categoryRepository,
                           DocumentMapper mapper) {
        this.documentRepository = documentRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Transactional
    public DocumentResponse create(DocumentCreateRequest request) {
        Category category = findCategory(request.categoryId());
        Document document = new Document();
        document.setTitle(request.title());
        document.setFilename(request.filename());
        document.setDescription(request.description());
        document.setCategory(category);
        return mapper.toResponse(documentRepository.saveAndFlush(document));
    }

    public List<DocumentResponse> getAll() {
        return documentRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    public DocumentResponse getById(Long id) {
        return mapper.toResponse(findDocument(id));
    }

    @Transactional
    public DocumentResponse update(Long id, DocumentUpdateRequest request) {
        Document document = findDocument(id);
        Category category = findCategory(request.categoryId());
        document.setTitle(request.title());
        document.setFilename(request.filename());
        document.setDescription(request.description());
        document.setCategory(category);
        return mapper.toResponse(documentRepository.saveAndFlush(document));
    }

    @Transactional
    public void delete(Long id) {
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
