package com.siva.shopsphere.inventory.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.siva.shopsphere.inventory.entity.Inventory;

import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<Inventory> findByProductId(UUID productId);
    boolean existsByProductId(UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.product.id = :productId")
    Optional<Inventory> findByProductIdWithLock(@Param("productId") UUID productId);

    @Query("select i from Inventory i where (i.quantity - i.reservedQuantity) <= i.lowStockThreshold order by i.product.name asc")
    List<Inventory> findLowStockInventory();
}
