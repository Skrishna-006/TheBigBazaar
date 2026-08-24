package com.siva.shopsphere.inventory.dto;

import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
    UUID id,
    UUID productId,
    String productName,
    String sku,
    int quantity,
    int reservedQuantity,
    int availableQuantity,
    int lowStockThreshold,
    boolean lowStock,
    Instant createdAt,
    Instant updatedAt
) {
}
