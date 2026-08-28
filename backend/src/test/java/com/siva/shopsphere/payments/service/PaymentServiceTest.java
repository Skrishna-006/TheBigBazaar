package com.siva.shopsphere.payments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.orders.entity.Order;
import com.siva.shopsphere.orders.entity.OrderStatus;
import com.siva.shopsphere.orders.repository.OrderRepository;
import com.siva.shopsphere.orders.service.OrderService;
import com.siva.shopsphere.payments.dto.InitiatePaymentRequest;
import com.siva.shopsphere.payments.dto.PaymentResponse;
import com.siva.shopsphere.payments.entity.Payment;
import com.siva.shopsphere.payments.entity.PaymentMethod;
import com.siva.shopsphere.payments.entity.PaymentStatus;
import com.siva.shopsphere.payments.gateway.PaymentGateway;
import com.siva.shopsphere.payments.repository.PaymentRepository;
import com.siva.shopsphere.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private CurrentUserService currentUserService;
    @Mock private PaymentGateway paymentGateway;
    @Mock private OrderService orderService;

    @InjectMocks private PaymentService paymentService;

    @Test
    void successfulPaymentConfirmsOrder() {
        User user = user();
        Order order = order(user, OrderStatus.PENDING, new BigDecimal("499.00"));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserId(order.getId(), user.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getId())).thenReturn(Optional.empty());
        when(paymentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.process(any(), any(), any(), any(), any()))
            .thenReturn(new PaymentGateway.PaymentGatewayResult(PaymentStatus.PAID, "prov_123", null));

        PaymentResponse response = paymentService.initiatePayment(order.getId(), new InitiatePaymentRequest(PaymentMethod.CARD), "idem-1");

        assertThat(response.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).save(order);
    }

    @Test
    void failedPaymentCancelsOrder() {
        User user = user();
        Order order = order(user, OrderStatus.PENDING, new BigDecimal("499.00"));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserId(order.getId(), user.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(order.getId())).thenReturn(Optional.empty());
        when(paymentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.process(any(), any(), any(), any(), any()))
            .thenReturn(new PaymentGateway.PaymentGatewayResult(PaymentStatus.FAILED, "prov_123", "Gateway declined"));
        when(orderService.cancelOrder(order.getId())).thenReturn(null);

        PaymentResponse response = paymentService.initiatePayment(order.getId(), new InitiatePaymentRequest(PaymentMethod.UPI), "idem-2");

        assertThat(response.status()).isEqualTo(PaymentStatus.FAILED);
        verify(orderService).cancelOrder(order.getId());
    }

    @Test
    void cancelledOrderCannotBePaid() {
        User user = user();
        Order order = order(user, OrderStatus.CANCELLED, new BigDecimal("499.00"));
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserId(order.getId(), user.getId())).thenReturn(Optional.of(order));

        assertThrows(ConflictException.class, () ->
            paymentService.initiatePayment(order.getId(), new InitiatePaymentRequest(PaymentMethod.CARD), "idem-3"));
    }

    @Test
    void missingOrderReturnsNotFound() {
        User user = user();
        UUID orderId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserId(eq(orderId), eq(user.getId()))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
            paymentService.initiatePayment(orderId, new InitiatePaymentRequest(PaymentMethod.CARD), "idem-4"));
    }

    private User user() {
        User user = new User();
        user.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        user.setEmail("customer@example.com");
        user.setFirstName("Siva");
        user.setLastName("Krishna");
        user.setRole(UserRole.CUSTOMER);
        return user;
    }

    private Order order(User user, OrderStatus status, BigDecimal total) {
        Order order = new Order();
        order.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        order.setUser(user);
        order.setStatus(status);
        order.setTotalAmount(total);
        order.setSubtotal(total);
        order.setShippingAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShippingFullName("Siva Krishna");
        order.setShippingPhoneNumber("9876543210");
        order.setShippingAddressLine1("Main Road");
        order.setShippingCity("Anantapur");
        order.setShippingState("AP");
        order.setShippingPostalCode("515001");
        order.setShippingCountry("India");
        return order;
    }
}
