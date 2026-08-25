package com.siva.shopsphere.orders.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.siva.shopsphere.orders.entity.OrderStatus;

public record OrderResponse(
    UUID id,
    OrderStatus status,
    List<OrderItemResponse> items,
    BigDecimal subtotal,
    BigDecimal shippingAmount,
    BigDecimal discountAmount,
    BigDecimal totalAmount,
    ShippingAddressSnapshot shippingAddress,
    Instant createdAt,
    Instant updatedAt
) {
    public record ShippingAddressSnapshot(
        UUID addressId,
        String fullName,
        String phoneNumber,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country
    ) {}
}
