package com.siva.shopsphere.products.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String name,
    String slug,
    String sku,
    String description,
    BigDecimal price,
    ProductCategoryResponse category,
    ProductBrandResponse brand,
    String imageUrl,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}
