package com.siva.shopsphere.inventory.dto;

import java.time.Instant;
import java.util.UUID;

import com.siva.shopsphere.inventory.entity.InventoryMovementType;

public record InventoryMovementResponse(
    UUID id,
    InventoryMovementType type,
    int quantity,
    String reason,
    Instant createdAt
) {
}
