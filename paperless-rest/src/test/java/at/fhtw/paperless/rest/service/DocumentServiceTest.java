package at.fhtw.paperless.rest.service;

import at.fhtw.paperless.rest.dto.*;
import at.fhtw.paperless.rest.entity.*;
import at.fhtw.paperless.rest.exception.*;
import at.fhtw.paperless.rest.mapper.DocumentMapper;
import at.fhtw.paperless.rest.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    @Mock DocumentRepository documents;
    @Mock CategoryRepository categories;
    DocumentService service;

    @BeforeEach
    void setUp() {
        service = new DocumentService(documents, categories, new DocumentMapper());
    }

    private void stubSave() {
        when(documents.saveAndFlush(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            document.setId(1L);
            return document;
        });
    }

    @Test
    void createWithoutCategory() {
        stubSave();
        var response = service.create(new DocumentCreateRequest("Invoice", "invoice.pdf", "Details", null));
        assertEquals(1L, response.id());
        assertEquals("Invoice", response.title());
        assertEquals("invoice.pdf", response.filename());
        assertEquals("Details", response.description());
        assertNull(response.categoryId());
        verifyNoInteractions(categories);
    }

    @Test
    void createWithExistingCategory() {
        Category category = new Category();
        category.setId(2L);
        category.setName("Finance");
        when(categories.findById(2L)).thenReturn(Optional.of(category));
        stubSave();
        var response = service.create(new DocumentCreateRequest("Invoice", "invoice.pdf", null, 2L));
        assertEquals(2L, response.categoryId());
        assertEquals("Finance", response.categoryName());
    }

    @Test
    void createWithMissingCategoryFailsWithoutSaving() {
        when(categories.findById(99L)).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class,
                () -> service.create(new DocumentCreateRequest("Invoice", "invoice.pdf", null, 99L)));
        verify(documents, never()).saveAndFlush(any());
    }

    @Test
    void getExistingDocument() {
        Document document = new Document();
        document.setId(1L);
        document.setTitle("Invoice");
        when(documents.findById(1L)).thenReturn(Optional.of(document));
        assertEquals("Invoice", service.getById(1L).title());
    }

    @Test
    void getMissingDocument() {
        assertThrows(DocumentNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void getAllDocuments() {
        Document document = new Document();
        document.setId(1L);
        when(documents.findAll()).thenReturn(List.of(document));
        assertEquals(List.of(1L), service.getAll().stream().map(DocumentResponse::id).toList());
    }

    @Test
    void updateDocumentAndAssignCategory() {
        Document document = new Document();
        document.setId(1L);
        Category category = new Category();
        category.setId(2L);
        when(documents.findById(1L)).thenReturn(Optional.of(document));
        when(categories.findById(2L)).thenReturn(Optional.of(category));
        stubSave();
        var response = service.update(1L, new DocumentUpdateRequest("Updated", "new.pdf", "New details", 2L));
        assertEquals("Updated", response.title());
        assertEquals("new.pdf", response.filename());
        assertEquals("New details", response.description());
        assertSame(category, document.getCategory());
    }

    @Test
    void updateCanRemoveCategoryAndDescription() {
        Document document = new Document();
        document.setCategory(new Category());
        document.setDescription("Old description");
        when(documents.findById(1L)).thenReturn(Optional.of(document));
        stubSave();
        var response = service.update(1L, new DocumentUpdateRequest("Updated", "new.pdf", null, null));
        assertNull(response.categoryId());
        assertNull(response.description());
        assertNull(document.getCategory());
        verifyNoInteractions(categories);
    }

    @Test
    void updateWithMissingCategoryDoesNotMutateDocument() {
        Document document = new Document();
        document.setTitle("Original");
        when(documents.findById(1L)).thenReturn(Optional.of(document));
        assertThrows(CategoryNotFoundException.class,
                () -> service.update(1L, new DocumentUpdateRequest("Changed", "new.pdf", null, 99L)));
        assertEquals("Original", document.getTitle());
        verify(documents, never()).saveAndFlush(any());
    }

    @Test
    void updateMissingDocument() {
        assertThrows(DocumentNotFoundException.class,
                () -> service.update(99L, new DocumentUpdateRequest("Updated", "new.pdf", null, null)));
        verify(documents, never()).saveAndFlush(any());
    }

    @Test
    void deleteExistingDocument() {
        Document document = new Document();
        when(documents.findById(1L)).thenReturn(Optional.of(document));
        service.delete(1L);
        verify(documents).delete(document);
    }

    @Test
    void deleteMissingDocument() {
        assertThrows(DocumentNotFoundException.class, () -> service.delete(99L));
        verify(documents, never()).delete(any());
    }
}
