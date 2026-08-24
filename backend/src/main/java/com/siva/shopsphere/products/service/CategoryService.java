package com.siva.shopsphere.products.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.products.dto.CategoryResponse;
import com.siva.shopsphere.products.dto.CreateCategoryRequest;
import com.siva.shopsphere.products.dto.UpdateCategoryRequest;
import com.siva.shopsphere.products.entity.Category;
import com.siva.shopsphere.products.mapper.CategoryMapper;
import com.siva.shopsphere.products.repository.CategoryRepository;
import com.siva.shopsphere.products.util.SlugUtils;
import com.siva.shopsphere.security.CurrentUserService;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public CategoryService(CategoryRepository categoryRepository, CurrentUserService currentUserService) {
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String name = normalizeName(request.name());
        ensureNameAndSlugAvailable(name, SlugUtils.toSlug(name), null);
        Category category = CategoryMapper.toEntity(request);
        category.setName(name);
        category.setSlug(SlugUtils.toSlug(name));
        category.setActive(true);
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAllByActiveTrueOrderByNameAsc().stream()
            .map(CategoryMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.isActive() && !isAdmin()) {
            throw new ResourceNotFoundException("Category not found");
        }
        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        String name = normalizeName(request.name());
        String slug = SlugUtils.toSlug(name);
        ensureNameAndSlugAvailable(name, slug, category.getId());
        CategoryMapper.updateEntity(category, request);
        category.setName(name);
        category.setSlug(slug);
        if (request.active() != null) {
            category.setActive(request.active());
        }
        return CategoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deactivateCategory(UUID id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setActive(false);
        categoryRepository.save(category);
    }

    private void ensureNameAndSlugAvailable(String name, String slug, UUID currentId) {
        categoryRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ConflictException("Category with this name already exists");
            }
        });
        categoryRepository.findBySlug(slug).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ConflictException("Category with this slug already exists");
            }
        });
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private boolean isAdmin() {
        try {
            return currentUserService.getCurrentUser().getRole() == UserRole.ADMIN;
        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }
}
