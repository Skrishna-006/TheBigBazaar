package com.siva.shopsphere.cart.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CartResponse(
    UUID cartId,
    List<CartItemResponse> items,
    int totalItemCount,
    BigDecimal subtotal,
    Instant createdAt,
    Instant updatedAt
) {
}
