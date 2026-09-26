package at.fhtw.paperless.rest.service;

import at.fhtw.paperless.rest.dto.*;
import at.fhtw.paperless.rest.entity.Category;
import at.fhtw.paperless.rest.exception.CategoryNotFoundException;
import at.fhtw.paperless.rest.mapper.CategoryMapper;
import at.fhtw.paperless.rest.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock CategoryRepository categories;
    @Mock DocumentRepository documents;
    CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(categories, documents, new CategoryMapper());
    }

    @Test
    void createCategory() {
        when(categories.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(1L);
            return category;
        });
        var response = service.create(new CategoryCreateRequest("Finance", "Invoices"));
        assertEquals(1L, response.id());
        assertEquals("Finance", response.name());
        assertEquals("Invoices", response.description());
    }

    @Test
    void getExistingCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Finance");
        when(categories.findById(1L)).thenReturn(Optional.of(category));
        assertEquals("Finance", service.getById(1L).name());
    }

    @Test
    void getMissingCategory() {
        assertThrows(CategoryNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void getAllCategories() {
        Category category = new Category();
        category.setId(1L);
        when(categories.findAll()).thenReturn(List.of(category));
        assertEquals(List.of(1L), service.getAll().stream().map(CategoryResponse::id).toList());
    }

    @Test
    void updateCategory() {
        Category category = new Category();
        category.setId(1L);
        when(categories.findById(1L)).thenReturn(Optional.of(category));
        when(categories.save(category)).thenReturn(category);
        var response = service.update(1L, new CategoryUpdateRequest("Updated", "New description"));
        assertEquals("Updated", response.name());
        assertEquals("New description", response.description());
        verify(categories).save(category);
    }

    @Test
    void updateMissingCategory() {
        assertThrows(CategoryNotFoundException.class,
                () -> service.update(99L, new CategoryUpdateRequest("Updated", null)));
        verify(categories, never()).save(any());
    }

    @Test
    void deleteUnusedCategory() {
        Category category = new Category();
        when(categories.findById(1L)).thenReturn(Optional.of(category));
        service.delete(1L);
        verify(categories).delete(category);
        verify(categories).flush();
    }

    @Test
    void deleteMissingCategory() {
        assertThrows(CategoryNotFoundException.class, () -> service.delete(99L));
        verify(categories, never()).delete(any());
    }

    @Test
    void cannotDeleteCategoryUsedByDocuments() {
        when(categories.findById(1L)).thenReturn(Optional.of(new Category()));
        when(documents.existsByCategoryId(1L)).thenReturn(true);
        assertThrows(DataIntegrityViolationException.class, () -> service.delete(1L));
        verify(categories, never()).delete(any());
    }
}
