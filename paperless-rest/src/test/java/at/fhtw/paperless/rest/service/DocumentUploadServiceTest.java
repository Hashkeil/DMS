package at.fhtw.paperless.rest.service;

import at.fhtw.paperless.rest.dto.DocumentCreateRequest;
import at.fhtw.paperless.rest.entity.*;
import at.fhtw.paperless.rest.exception.*;
import at.fhtw.paperless.rest.mapper.DocumentMapper;
import at.fhtw.paperless.rest.messaging.*;
import at.fhtw.paperless.rest.repository.*;
import at.fhtw.paperless.rest.storage.DocumentStorage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.*;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentUploadServiceTest {
    @Mock DocumentRepository documents;
    @Mock CategoryRepository categories;
    @Mock DocumentStorage storage;
    @Mock DocumentEventPublisher publisher;
    @Mock at.fhtw.paperless.rest.service.LabelService labelService;
    DocumentUploadService service;
    final MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "%PDF-1.4".getBytes());
    final DocumentCreateRequest request = new DocumentCreateRequest("Test", "test.pdf", null, null, List.of());

    @BeforeEach void setup() {
        service = new DocumentUploadService(documents, categories,
            org.mapstruct.factory.Mappers.getMapper(DocumentMapper.class), storage, publisher, labelService);
    }

    void storedDocument() {
        when(storage.store(file)).thenReturn("key.pdf");
        when(documents.saveAndFlush(any())).thenAnswer(call -> {
            Document document = call.getArgument(0);
            document.setId(5L);
            return document;
        });
    }

    @Test void storesMetadataAndPublishesReference() {
        storedDocument();
        assertEquals(5L, service.upload(request, file).id());
        verify(publisher).publish(new DocumentUploadedEvent(5L, "test.pdf", "key.pdf"));
        var capture = ArgumentCaptor.forClass(Document.class);
        verify(documents).saveAndFlush(capture.capture());
        assertEquals("key.pdf", capture.getValue().getStorageKey());
        verify(storage, never()).delete(any());
    }

    @Test void usesExistingCategory() {
        storedDocument();
        Category category = new Category();
        category.setId(2L);
        when(categories.findById(2L)).thenReturn(Optional.of(category));
        assertEquals(2L, service.upload(new DocumentCreateRequest("Test", "test.pdf", null, 2L, List.of()), file).categoryId());
    }

    @Test void rejectsMissingCategoryBeforeStorage() {
        assertThrows(CategoryNotFoundException.class,
            () -> service.upload(new DocumentCreateRequest("Test", "test.pdf", null, 99L, List.of()), file));
        verifyNoInteractions(storage, publisher, documents);
    }

    @Test void storageFailureDoesNotPublishOrPersist() {
        when(storage.store(file)).thenThrow(new DocumentStorageException("disk"));
        assertThrows(DocumentStorageException.class, () -> service.upload(request, file));
        verifyNoInteractions(documents, publisher);
    }

    @Test void publishFailureCleansUpFile() {
        storedDocument();
        doThrow(new DocumentPublishException("broker down")).when(publisher).publish(any());
        assertThrows(DocumentPublishException.class, () -> service.upload(request, file));
        verify(storage).delete("key.pdf");
    }

    @Test void databaseFailureCleansUpWithoutPublishing() {
        when(storage.store(file)).thenReturn("key.pdf");
        when(documents.saveAndFlush(any())).thenThrow(new IllegalStateException("database"));
        assertThrows(IllegalStateException.class, () -> service.upload(request, file));
        verify(storage).delete("key.pdf");
        verifyNoInteractions(publisher);
    }

    @Test void transactionRollbackCleansUpFile() {
        storedDocument();
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.upload(request, file);
            TransactionSynchronizationManager.getSynchronizations().forEach(
                sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
            verify(storage).delete("key.pdf");
        } finally { TransactionSynchronizationManager.clearSynchronization(); }
    }

    @Test void committedTransactionKeepsFile() {
        storedDocument();
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.upload(request, file);
            TransactionSynchronizationManager.getSynchronizations().forEach(
                sync -> sync.afterCompletion(TransactionSynchronization.STATUS_COMMITTED));
            verify(storage, never()).delete(any());
        } finally { TransactionSynchronizationManager.clearSynchronization(); }
    }
}
