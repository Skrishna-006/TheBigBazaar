package com.siva.shopsphere.payments.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.siva.shopsphere.payments.entity.PaymentMethod;
import com.siva.shopsphere.payments.entity.PaymentStatus;

public record PaymentResponse(
    UUID paymentId,
    UUID orderId,
    BigDecimal amount,
    String currency,
    PaymentStatus status,
    PaymentMethod paymentMethod,
    String providerReference,
    String failureReason,
    Instant createdAt,
    Instant updatedAt
) {}
