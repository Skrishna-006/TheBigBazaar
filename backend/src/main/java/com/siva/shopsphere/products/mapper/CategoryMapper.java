package com.siva.shopsphere.products.mapper;

import com.siva.shopsphere.products.dto.CategoryResponse;
import com.siva.shopsphere.products.dto.CreateCategoryRequest;
import com.siva.shopsphere.products.dto.UpdateCategoryRequest;
import com.siva.shopsphere.products.entity.Category;

public final class CategoryMapper {
    private CategoryMapper() {
    }

    public static Category toEntity(CreateCategoryRequest request) {
        Category category = new Category();
        apply(category, request.name(), request.description(), request.imageUrl(), null);
        return category;
    }

    public static void updateEntity(Category category, UpdateCategoryRequest request) {
        apply(category, request.name(), request.description(), request.imageUrl(), request.active());
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getSlug(),
            category.getDescription(),
            category.getImageUrl(),
            category.isActive(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }

    private static void apply(Category category, String name, String description, String imageUrl, Boolean active) {
        if (name != null) {
            category.setName(name.trim());
        }
        category.setDescription(description == null ? null : description.trim());
        category.setImageUrl(imageUrl == null ? null : imageUrl.trim());
        if (active != null) {
            category.setActive(active);
        }
    }
}
