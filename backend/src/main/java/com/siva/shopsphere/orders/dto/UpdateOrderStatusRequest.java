package com.siva.shopsphere.orders.dto;

import com.siva.shopsphere.orders.entity.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
    @NotNull(message = "Status is required")
    OrderStatus status
) {
}
