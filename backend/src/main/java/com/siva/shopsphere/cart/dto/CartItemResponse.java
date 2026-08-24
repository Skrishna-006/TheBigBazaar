package com.siva.shopsphere.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.siva.shopsphere.products.dto.ProductBrandResponse;
import com.siva.shopsphere.products.dto.ProductCategoryResponse;

public record CartItemResponse(
    UUID productId,
    String productName,
    String productImageUrl,
    ProductCategoryResponse category,
    ProductBrandResponse brand,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal,
    boolean available
) {
}
