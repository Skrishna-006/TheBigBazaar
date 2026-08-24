package com.siva.shopsphere.inventory.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siva.shopsphere.inventory.entity.InventoryMovement;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {
    List<InventoryMovement> findByInventoryIdOrderByCreatedAtDesc(UUID inventoryId);
}
