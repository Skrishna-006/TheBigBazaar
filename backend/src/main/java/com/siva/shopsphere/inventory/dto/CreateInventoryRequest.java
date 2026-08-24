package com.siva.shopsphere.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateInventoryRequest(
    @NotNull UUID productId,
    @NotNull @Min(0) Integer quantity,
    @NotNull @Min(0) Integer lowStockThreshold
) {
}
