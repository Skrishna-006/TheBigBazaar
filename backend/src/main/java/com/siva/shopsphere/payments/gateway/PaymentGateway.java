package com.siva.shopsphere.payments.gateway;

import java.math.BigDecimal;

import com.siva.shopsphere.payments.entity.PaymentMethod;
import com.siva.shopsphere.payments.entity.PaymentStatus;

public interface PaymentGateway {
    PaymentGatewayResult process(String orderId, BigDecimal amount, String currency, PaymentMethod method, String idempotencyKey);

    record PaymentGatewayResult(PaymentStatus status, String providerReference, String failureReason) {}
}
