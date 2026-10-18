package at.fhtw.paperless.rest.service;

import at.fhtw.paperless.rest.dto.*;
import at.fhtw.paperless.rest.entity.Document;
import at.fhtw.paperless.rest.exception.*;
import at.fhtw.paperless.rest.mapper.DocumentMapper;
import at.fhtw.paperless.rest.messaging.*;
import at.fhtw.paperless.rest.repository.*;
import at.fhtw.paperless.rest.storage.DocumentStorage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

@Service
@Validated
public class DocumentUploadService {
    private static final Logger log = LoggerFactory.getLogger(DocumentUploadService.class);
    private final DocumentRepository documents;
    private final CategoryRepository categories;
    private final DocumentMapper mapper;
    private final DocumentStorage storage;
    private final DocumentEventPublisher publisher;
    private final LabelService labelService;

    public DocumentUploadService(DocumentRepository documents, CategoryRepository categories,
                                 DocumentMapper mapper, DocumentStorage storage, DocumentEventPublisher publisher,
                                 LabelService labelService) {
        this.documents = documents;
        this.categories = categories;
        this.mapper = mapper;
        this.storage = storage;
        this.publisher = publisher;
        this.labelService = labelService;
    }

    @Transactional
    public DocumentResponse upload(@NotNull @Valid DocumentCreateRequest request, MultipartFile file) {
        var category = request.categoryId() == null ? null : categories.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
        String key = storage.store(file);
        boolean synchronizedCleanup = TransactionSynchronizationManager.isSynchronizationActive();
        if (synchronizedCleanup) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) cleanup(key);
                }
            });
        }
        try {
            Document document = new Document();
            document.setTitle(request.title());
            document.setFilename(request.filename());
            document.setDescription(request.description());
            document.setCategory(category);
            document.setStorageKey(key);
            document.setLabels(labelService.resolve(request.labels()));
            document = documents.saveAndFlush(document);
            publisher.publish(new DocumentUploadedEvent(document.getId(), document.getFilename(), key));
            log.info("PDF stored and queued: documentId={}", document.getId());
            return mapper.toResponse(document);
        } catch (RuntimeException failure) {
            if (!synchronizedCleanup) cleanup(key);
            throw failure;
        }
    }

    private void cleanup(String key) {
        try { storage.delete(key); }
        catch (RuntimeException failure) { log.error("Failed to clean up rolled-back upload: storageKey={}", key, failure); }
    }
}
