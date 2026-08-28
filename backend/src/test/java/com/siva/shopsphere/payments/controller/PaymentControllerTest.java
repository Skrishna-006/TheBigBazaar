package com.siva.shopsphere.payments.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.web.servlet.MockMvc;

import com.siva.shopsphere.config.SecurityConfig;
import com.siva.shopsphere.config.WebConfig;
import com.siva.shopsphere.payments.dto.PaymentResponse;
import com.siva.shopsphere.payments.entity.PaymentMethod;
import com.siva.shopsphere.payments.entity.PaymentStatus;
import com.siva.shopsphere.payments.service.PaymentService;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(PaymentController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private PaymentService paymentService;
    @MockBean private AuthenticationProvider authenticationProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private JwtService jwtService;

    @Test
    void unauthenticatedCreatePaymentReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/payments/orders/22222222-2222-2222-2222-222222222222")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CARD\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedCreatePaymentReturnsCreated() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(paymentService.initiatePayment(any(), any(), any())).thenReturn(paymentResponse());

        mockMvc.perform(post("/api/v1/payments/orders/22222222-2222-2222-2222-222222222222")
                .header("Authorization", "Bearer token")
                .header("Idempotency-Key", "idem-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CARD\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void authenticatedGetPaymentReturnsOk() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(paymentService.getPayment(UUID.fromString("33333333-3333-3333-3333-333333333333"))).thenReturn(paymentResponse());

        mockMvc.perform(get("/api/v1/payments/33333333-3333-3333-3333-333333333333")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isOk());
    }

    private void authenticate(String token, String email, String role) {
        when(jwtService.isAccessTokenValid(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(email);
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(
            org.springframework.security.core.userdetails.User.withUsername(email)
                .password("hash")
                .roles(role)
                .build()
        );
    }

    private PaymentResponse paymentResponse() {
        return new PaymentResponse(
            UUID.fromString("33333333-3333-3333-3333-333333333333"),
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            new BigDecimal("499.00"),
            "INR",
            PaymentStatus.PAID,
            PaymentMethod.CARD,
            "prov_123",
            null,
            Instant.now(),
            Instant.now()
        );
    }
}
