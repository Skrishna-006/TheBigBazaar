package com.siva.shopsphere.products.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String name,
    String slug,
    String sku,
    String description,
    BigDecimal price,
    BigDecimal originalPrice,
    BigDecimal deliveryCharge,
    Integer deliveryDays,
    BigDecimal rating,
    Integer reviewCount,
    ProductCategoryResponse category,
    ProductBrandResponse brand,
    ProductSellerResponse seller,
    List<String> highlights,
    java.util.Map<String, Object> specifications,
    String imageUrl,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}
