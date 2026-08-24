package com.siva.shopsphere.cart.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siva.shopsphere.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);
    List<CartItem> findAllByCartIdOrderByCreatedAtAsc(UUID cartId);
    void deleteAllByCartId(UUID cartId);
    void deleteByCartIdAndProductId(UUID cartId, UUID productId);
}
