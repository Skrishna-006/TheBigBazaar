package com.siva.shopsphere.inventory.mapper;

import com.siva.shopsphere.inventory.dto.InventoryMovementResponse;
import com.siva.shopsphere.inventory.dto.InventoryResponse;
import com.siva.shopsphere.inventory.entity.Inventory;
import com.siva.shopsphere.inventory.entity.InventoryMovement;

public final class InventoryMapper {
    private InventoryMapper() {
    }

    public static InventoryResponse toResponse(Inventory inventory) {
        int availableQuantity = inventory.getQuantity() - inventory.getReservedQuantity();
        return new InventoryResponse(
            inventory.getId(),
            inventory.getProduct().getId(),
            inventory.getProduct().getName(),
            inventory.getProduct().getSku(),
            inventory.getQuantity(),
            inventory.getReservedQuantity(),
            availableQuantity,
            inventory.getLowStockThreshold(),
            availableQuantity <= inventory.getLowStockThreshold(),
            inventory.getCreatedAt(),
            inventory.getUpdatedAt()
        );
    }

    public static InventoryMovementResponse toResponse(InventoryMovement movement) {
        return new InventoryMovementResponse(
            movement.getId(),
            movement.getType(),
            movement.getQuantity(),
            movement.getReason(),
            movement.getCreatedAt()
        );
    }
}
