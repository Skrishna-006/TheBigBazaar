package com.siva.shopsphere.products.mapper;

import com.siva.shopsphere.products.dto.CreateProductRequest;
import com.siva.shopsphere.products.dto.ProductBrandResponse;
import com.siva.shopsphere.products.dto.ProductCategoryResponse;
import com.siva.shopsphere.products.dto.ProductResponse;
import com.siva.shopsphere.products.dto.UpdateProductRequest;
import com.siva.shopsphere.products.entity.Product;

public final class ProductMapper {
    private ProductMapper() {
    }

    public static Product toEntity(CreateProductRequest request) {
        Product product = new Product();
        apply(product, request.name(), request.description(), request.price(), request.imageUrl(), null);
        return product;
    }

    public static void updateEntity(Product product, UpdateProductRequest request) {
        apply(product, request.name(), request.description(), request.price(), request.imageUrl(), request.active());
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getSlug(),
            product.getSku(),
            product.getDescription(),
            product.getPrice(),
            new ProductCategoryResponse(
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCategory().getSlug()
            ),
            new ProductBrandResponse(
                product.getBrand().getId(),
                product.getBrand().getName(),
                product.getBrand().getSlug()
            ),
            product.getImageUrl(),
            product.isActive(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    private static void apply(Product product, String name, String description, java.math.BigDecimal price, String imageUrl, Boolean active) {
        if (name != null) {
            product.setName(name.trim());
        }
        product.setDescription(description == null ? null : description.trim());
        product.setPrice(price);
        product.setImageUrl(imageUrl == null ? null : imageUrl.trim());
        if (active != null) {
            product.setActive(active);
        }
    }
}
