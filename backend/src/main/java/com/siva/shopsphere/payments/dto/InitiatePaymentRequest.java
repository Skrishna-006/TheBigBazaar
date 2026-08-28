package com.siva.shopsphere.payments.dto;

import com.siva.shopsphere.payments.entity.PaymentMethod;

import jakarta.validation.constraints.NotNull;

public record InitiatePaymentRequest(
    @NotNull PaymentMethod paymentMethod
) {}
