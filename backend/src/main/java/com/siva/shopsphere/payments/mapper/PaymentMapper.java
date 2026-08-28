package com.siva.shopsphere.payments.mapper;

import com.siva.shopsphere.payments.dto.PaymentResponse;
import com.siva.shopsphere.payments.entity.Payment;

public final class PaymentMapper {
    private PaymentMapper() {}

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getOrder().getId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getStatus(),
            payment.getPaymentMethod(),
            payment.getProviderReference(),
            payment.getFailureReason(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }
}
