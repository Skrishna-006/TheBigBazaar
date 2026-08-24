package com.siva.shopsphere.products.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.products.dto.CreateCategoryRequest;
import com.siva.shopsphere.products.dto.UpdateCategoryRequest;
import com.siva.shopsphere.products.entity.Category;
import com.siva.shopsphere.products.repository.CategoryRepository;
import com.siva.shopsphere.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategoryGeneratesSlugAndActivatesCategory() {
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = categoryService.createCategory(new CreateCategoryRequest("Electronics", "Devices", "https://example.com/electronics.jpg"));

        assertThat(response.slug()).isEqualTo("electronics");
        assertThat(response.active()).isTrue();
    }

    @Test
    void duplicateNameIsRejected() {
        Category existing = new Category();
        existing.setId(UUID.randomUUID());
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> categoryService.createCategory(new CreateCategoryRequest("Electronics", null, null)))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void duplicateSlugIsRejected() {
        Category existing = new Category();
        existing.setId(UUID.randomUUID());
        when(categoryRepository.findByNameIgnoreCase("Home Kitchen")).thenReturn(Optional.empty());
        when(categoryRepository.findBySlug("home-kitchen")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> categoryService.createCategory(new CreateCategoryRequest("Home Kitchen", null, null)))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void listReturnsActiveCategoriesOnly() {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Books");
        category.setSlug("books");
        category.setActive(true);
        when(categoryRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(category));

        assertThat(categoryService.getActiveCategories()).hasSize(1);
    }

    @Test
    void updateRegeneratesSlugWhenNameChanges() {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setActive(true);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Consumer Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.findBySlug("consumer-electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = categoryService.updateCategory(category.getId(), new UpdateCategoryRequest("Consumer Electronics", "Updated", null, true));

        assertThat(response.slug()).isEqualTo("consumer-electronics");
    }

    @Test
    void deactivateCategorySetsActiveFalse() {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setActive(true);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.deactivateCategory(category.getId());

        verify(categoryRepository).save(category);
        assertThat(category.isActive()).isFalse();
    }
}
