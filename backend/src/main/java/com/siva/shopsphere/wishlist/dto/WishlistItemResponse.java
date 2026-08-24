package com.siva.shopsphere.wishlist.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.siva.shopsphere.products.dto.ProductBrandResponse;
import com.siva.shopsphere.products.dto.ProductCategoryResponse;

public record WishlistItemResponse(
    UUID productId,
    String productName,
    String slug,
    String imageUrl,
    BigDecimal price,
    ProductCategoryResponse category,
    ProductBrandResponse brand,
    boolean available,
    Instant createdAt
) {
}
