package com.siva.shopsphere.payments.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.siva.shopsphere.payments.dto.InitiatePaymentRequest;
import com.siva.shopsphere.payments.dto.PaymentResponse;
import com.siva.shopsphere.payments.service.PaymentService;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/orders/{orderId}")
    public ResponseEntity<PaymentResponse> initiate(@PathVariable UUID orderId, @Valid @RequestBody InitiatePaymentRequest request, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePayment(orderId, request, idempotencyKey == null ? orderId.toString() : idempotencyKey));
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    @GetMapping("/orders/{orderId}/payment")
    public ResponseEntity<PaymentResponse> getOrderPayment(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getOrderPayment(orderId));
    }
}
