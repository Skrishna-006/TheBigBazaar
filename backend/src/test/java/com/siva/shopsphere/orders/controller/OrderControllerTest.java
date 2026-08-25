package com.siva.shopsphere.orders.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
import com.siva.shopsphere.orders.dto.OrderItemResponse;
import com.siva.shopsphere.orders.dto.OrderResponse;
import com.siva.shopsphere.orders.dto.UpdateOrderStatusRequest;
import com.siva.shopsphere.orders.entity.OrderStatus;
import com.siva.shopsphere.orders.service.OrderService;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void unauthenticatedCreateOrderReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/orders"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedCreateOrderReturnsCreated() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(orderService.createOrder(any())).thenReturn(orderResponse(OrderStatus.PENDING));

        mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"addressId\":\"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void customerCanListOwnOrders() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(orderService.getMyOrders()).thenReturn(List.of(orderResponse(OrderStatus.CONFIRMED)));

        mockMvc.perform(get("/api/v1/orders").header("Authorization", "Bearer token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void customerCancelReturnsOk() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(orderService.cancelOrder(UUID.fromString("22222222-2222-2222-2222-222222222222"))).thenReturn(orderResponse(OrderStatus.CANCELLED));

        mockMvc.perform(post("/api/v1/orders/22222222-2222-2222-2222-222222222222/cancel")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void adminListReturnsOk() throws Exception {
        authenticate("token", "admin@example.com", "ADMIN");
        when(orderService.getAdminOrders(null)).thenReturn(List.of(orderResponse(OrderStatus.CONFIRMED)));

        mockMvc.perform(get("/api/v1/admin/orders").header("Authorization", "Bearer token"))
            .andExpect(status().isOk());
    }

    @Test
    void customerCannotAccessAdminOrders() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");

        mockMvc.perform(get("/api/v1/admin/orders").header("Authorization", "Bearer token"))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminStatusUpdateReturnsOk() throws Exception {
        authenticate("token", "admin@example.com", "ADMIN");
        when(orderService.updateOrderStatus(any(), any())).thenReturn(orderResponse(OrderStatus.PROCESSING));

        mockMvc.perform(patch("/api/v1/admin/orders/22222222-2222-2222-2222-222222222222/status")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PROCESSING\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PROCESSING"));
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

    private OrderResponse orderResponse(OrderStatus status) {
        return new OrderResponse(
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            status,
            List.of(new OrderItemResponse(
                UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                "MacBook Air M3",
                "MBA-M3-256",
                "https://example.com/macbook.jpg",
                new BigDecimal("1000.00"),
                1,
                new BigDecimal("1000.00")
            )),
            new BigDecimal("1000.00"),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            new BigDecimal("1000.00"),
            new OrderResponse.ShippingAddressSnapshot(
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                "Siva Krishna",
                "9876543210",
                "Main Road",
                null,
                "Anantapur",
                "AP",
                "515001",
                "India"
            ),
            Instant.now(),
            Instant.now()
        );
    }
}
