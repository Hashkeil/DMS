package at.fhtw.paperless.rest.service;

import at.fhtw.paperless.rest.dto.*;
import at.fhtw.paperless.rest.entity.Category;
import at.fhtw.paperless.rest.exception.CategoryNotFoundException;
import at.fhtw.paperless.rest.mapper.CategoryMapper;
import at.fhtw.paperless.rest.repository.CategoryRepository;
import at.fhtw.paperless.rest.repository.DocumentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final DocumentRepository documentRepository;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository categoryRepository, DocumentRepository documentRepository,
                           CategoryMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.documentRepository = documentRepository;
        this.mapper = mapper;
    }

    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        return mapper.toResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    public CategoryResponse getById(Long id) {
        return mapper.toResponse(findCategory(id));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        Category category = findCategory(id);
        category.setName(request.name());
        category.setDescription(request.description());
        return mapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = findCategory(id);
        if (documentRepository.existsByCategoryId(id)) {
            throw new DataIntegrityViolationException("Category is still used by documents");
        }
        categoryRepository.delete(category);
        categoryRepository.flush();
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
