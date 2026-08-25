package com.siva.shopsphere.orders.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
    UUID productId,
    String productName,
    String productSku,
    String productImageUrl,
    BigDecimal unitPrice,
    int quantity,
    BigDecimal lineTotal
) {
}
