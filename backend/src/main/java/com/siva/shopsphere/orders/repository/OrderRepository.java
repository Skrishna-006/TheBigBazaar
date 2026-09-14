package com.siva.shopsphere.orders.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siva.shopsphere.orders.entity.Order;
import com.siva.shopsphere.orders.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);
    List<Order> findAllByStatusOrderByCreatedAtDesc(OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
    boolean existsByUserIdAndItemsProductId(UUID userId, UUID productId);
}
