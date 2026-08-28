package com.siva.shopsphere.payments.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.orders.entity.Order;
import com.siva.shopsphere.orders.entity.OrderStatus;
import com.siva.shopsphere.orders.repository.OrderRepository;
import com.siva.shopsphere.orders.service.OrderService;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.payments.dto.InitiatePaymentRequest;
import com.siva.shopsphere.payments.dto.PaymentResponse;
import com.siva.shopsphere.payments.entity.Payment;
import com.siva.shopsphere.payments.entity.PaymentMethod;
import com.siva.shopsphere.payments.entity.PaymentStatus;
import com.siva.shopsphere.payments.gateway.PaymentGateway;
import com.siva.shopsphere.payments.mapper.PaymentMapper;
import com.siva.shopsphere.payments.repository.PaymentRepository;
import com.siva.shopsphere.security.CurrentUserService;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CurrentUserService currentUserService;
    private final PaymentGateway paymentGateway;
    private final OrderService orderService;
    private final String currency;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository, CurrentUserService currentUserService, PaymentGateway paymentGateway, OrderService orderService, @Value("${app.payments.currency:INR}") String currency) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.currentUserService = currentUserService;
        this.paymentGateway = paymentGateway;
        this.orderService = orderService;
        this.currency = currency;
    }

    @Transactional
    public PaymentResponse initiatePayment(UUID orderId, InitiatePaymentRequest request, String idempotencyKey) {
        User user = currentUserService.getCurrentUser();
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException("Cancelled orders cannot be paid");
        }
        Payment existing = paymentRepository.findByOrderId(orderId).orElse(null);
        if (existing != null) {
            return PaymentMapper.toResponse(existing);
        }
        if (paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return PaymentMapper.toResponse(paymentRepository.findByIdempotencyKey(idempotencyKey).orElseThrow());
        }
        if (request.paymentMethod() == PaymentMethod.COD) {
            throw new BadRequestException("Cash on delivery is not supported for online payment initiation");
        }
        if (idempotencyKey != null) {
            Payment idempotent = paymentRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
            if (idempotent != null) {
                return PaymentMapper.toResponse(idempotent);
            }
        }
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(request.paymentMethod());
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency(currency);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIdempotencyKey(idempotencyKey);
        payment.setProviderName("development-mock");
        payment = paymentRepository.save(payment);

        var result = paymentGateway.process(orderId.toString(), order.getTotalAmount(), currency, request.paymentMethod(), idempotencyKey);
        payment.setStatus(result.status());
        payment.setProviderReference(result.providerReference());
        payment.setFailureReason(result.failureReason());
        paymentRepository.save(payment);

        if (result.status() == PaymentStatus.PAID) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        } else {
            orderService.cancelOrder(orderId);
        }
        return PaymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        ensureOwnership(payment);
        return PaymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getOrderPayment(UUID orderId) {
        User user = currentUserService.getCurrentUser();
        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (!payment.getOrder().getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new ResourceNotFoundException("Payment not found");
        }
        return PaymentMapper.toResponse(payment);
    }

    private void ensureOwnership(Payment payment) {
        User user = currentUserService.getCurrentUser();
        if (!payment.getOrder().getUser().getId().equals(user.getId()) && user.getRole() != UserRole.ADMIN) {
            throw new ResourceNotFoundException("Payment not found");
        }
    }
}
