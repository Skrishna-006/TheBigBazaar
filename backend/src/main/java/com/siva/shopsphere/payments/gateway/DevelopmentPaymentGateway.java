package com.siva.shopsphere.payments.gateway;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.siva.shopsphere.payments.entity.PaymentMethod;
import com.siva.shopsphere.payments.entity.PaymentStatus;

@Component
@Primary
public class DevelopmentPaymentGateway implements PaymentGateway {
    @Override
    public PaymentGatewayResult process(String orderId, BigDecimal amount, String currency, PaymentMethod method, String idempotencyKey) {
        return new PaymentGatewayResult(PaymentStatus.PAID, "dev_" + UUID.randomUUID(), null);
    }
}
