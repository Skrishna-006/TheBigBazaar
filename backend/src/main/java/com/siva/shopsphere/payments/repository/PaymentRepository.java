package com.siva.shopsphere.payments.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siva.shopsphere.payments.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    boolean existsByOrderId(UUID orderId);
}
