package com.siva.shopsphere.products.mapper;

import com.siva.shopsphere.products.dto.CreateProductRequest;
import com.siva.shopsphere.products.dto.ProductBrandResponse;
import com.siva.shopsphere.products.dto.ProductCategoryResponse;
import com.siva.shopsphere.products.dto.ProductResponse;
import com.siva.shopsphere.products.dto.ProductSellerResponse;
import com.siva.shopsphere.products.dto.UpdateProductRequest;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.sellers.entity.Seller;
import org.hibernate.Hibernate;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;

public final class ProductMapper {
    private ProductMapper() {
    }

    public static Product toEntity(CreateProductRequest request) {
        Product product = new Product();
        apply(
            product, request.name(), request.description(), request.price(), request.imageUrl(), null,
            request.originalPrice(), request.deliveryCharge(), request.deliveryDays(), request.rating(), request.reviewCount()
        );
        return product;
    }

    public static void updateEntity(Product product, UpdateProductRequest request) {
        apply(
            product, request.name(), request.description(), request.price(), request.imageUrl(), request.active(),
            request.originalPrice(), request.deliveryCharge(), request.deliveryDays(), request.rating(), request.reviewCount()
        );
    }

    public static ProductResponse toResponse(Product product) {
        ProductSellerResponse sellerResponse = null;
        if (Hibernate.isInitialized(product.getSeller()) && product.getSeller() != null) {
            Seller s = product.getSeller();
            sellerResponse = new ProductSellerResponse(
                s.getId(), s.getName(), s.getLogoUrl(), s.getRating(), s.getRatingCount(), s.getFollowerCount(), s.getDescription()
            );
        }

        List<String> highlights = product.getHighlights() != null ? product.getHighlights() : Collections.emptyList();
        java.util.Map<String, Object> specs = product.getSpecifications() != null ? product.getSpecifications() : java.util.Collections.emptyMap();

        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getSlug(),
            product.getSku(),
            product.getDescription(),
            product.getPrice(),
            product.getOriginalPrice(),
            product.getDeliveryCharge(),
            product.getDeliveryDays(),
            product.getRating(),
            product.getReviewCount(),
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
            sellerResponse,
            highlights,
            specs,
            product.getImageUrl(),
            product.isActive(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    public static ProductResponse toDetailResponse(Product product) {
        ProductSellerResponse sellerResponse = null;
        if (product.getSeller() != null) {
            Seller s = product.getSeller();
            sellerResponse = new ProductSellerResponse(
                s.getId(), s.getName(), s.getLogoUrl(), s.getRating(), s.getRatingCount(), s.getFollowerCount(), s.getDescription()
            );
        }

        List<String> highlights = product.getHighlights() != null ? product.getHighlights() : Collections.emptyList();
        java.util.Map<String, Object> specs = product.getSpecifications() != null ? product.getSpecifications() : java.util.Collections.emptyMap();

        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getSlug(),
            product.getSku(),
            product.getDescription(),
            product.getPrice(),
            product.getOriginalPrice(),
            product.getDeliveryCharge(),
            product.getDeliveryDays(),
            product.getRating(),
            product.getReviewCount(),
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
            sellerResponse,
            highlights,
            specs,
            product.getImageUrl(),
            product.isActive(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }

    private static void apply(
            Product product, String name, String description, java.math.BigDecimal price, String imageUrl, Boolean active,
            java.math.BigDecimal originalPrice, java.math.BigDecimal deliveryCharge, Integer deliveryDays,
            java.math.BigDecimal rating, Integer reviewCount) {
        if (name != null) {
            product.setName(name.trim());
        }
        product.setDescription(description == null ? null : description.trim());
        product.setPrice(price);
        product.setOriginalPrice(originalPrice);
        if (deliveryCharge != null) product.setDeliveryCharge(deliveryCharge);
        if (deliveryDays != null) product.setDeliveryDays(deliveryDays);
        product.setRating(rating);
        if (reviewCount != null) product.setReviewCount(reviewCount);
        product.setImageUrl(imageUrl == null ? null : imageUrl.trim());
        if (active != null) {
            product.setActive(active);
        }
    }
}
