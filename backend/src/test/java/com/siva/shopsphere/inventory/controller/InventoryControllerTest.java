package com.siva.shopsphere.inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.siva.shopsphere.inventory.dto.InventoryMovementResponse;
import com.siva.shopsphere.inventory.dto.InventoryResponse;
import com.siva.shopsphere.inventory.entity.InventoryMovementType;
import com.siva.shopsphere.inventory.service.InventoryService;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(InventoryController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void unauthenticatedGetIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/products/11111111-1111-1111-1111-111111111111"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customerGetIsForbidden() throws Exception {
        authenticate("customer-token", "customer@example.com", "CUSTOMER");
        mockMvc.perform(get("/api/v1/inventory/products/11111111-1111-1111-1111-111111111111")
                .header("Authorization", "Bearer customer-token"))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCreateReturnsCreated() throws Exception {
        authenticate("admin-token", "admin@example.com", "ADMIN");
        when(inventoryService.createInventory(any())).thenReturn(response());

        mockMvc.perform(post("/api/v1/inventory")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"productId":"11111111-1111-1111-1111-111111111111","quantity":100,"lowStockThreshold":10}
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    void adminAdjustReturnsOk() throws Exception {
        authenticate("admin-token", "admin@example.com", "ADMIN");
        when(inventoryService.adjustStock(any(), any())).thenReturn(response());

        mockMvc.perform(post("/api/v1/inventory/products/11111111-1111-1111-1111-111111111111/adjust")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"type":"STOCK_IN","quantity":10,"reason":"Shipment"}
                """))
            .andExpect(status().isOk());
    }

    @Test
    void adminLowStockReturnsOk() throws Exception {
        authenticate("admin-token", "admin@example.com", "ADMIN");
        when(inventoryService.getLowStockInventory()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/inventory/low-stock")
                .header("Authorization", "Bearer admin-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].availableQuantity").value(100));
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

    private InventoryResponse response() {
        return new InventoryResponse(
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "MacBook Air M3",
            "MBA-M3-256",
            100,
            0,
            100,
            10,
            false,
            Instant.now(),
            Instant.now()
        );
    }
}
