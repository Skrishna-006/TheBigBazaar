package com.siva.shopsphere.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockAdjustmentRequest(
    @NotNull Type type,
    @NotNull Integer quantity,
    @NotBlank @Size(max = 500) String reason
) {
    public enum Type {
        STOCK_IN,
        STOCK_OUT,
        ADJUSTMENT
    }
}
