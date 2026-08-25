package com.siva.shopsphere.orders.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
    @NotNull(message = "Address is required")
    UUID addressId
) {
}
