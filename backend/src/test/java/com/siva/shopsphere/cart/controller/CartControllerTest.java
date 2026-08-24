package com.siva.shopsphere.cart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.siva.shopsphere.cart.dto.CartItemResponse;
import com.siva.shopsphere.cart.dto.CartResponse;
import com.siva.shopsphere.cart.service.CartService;
import com.siva.shopsphere.config.SecurityConfig;
import com.siva.shopsphere.config.WebConfig;
import com.siva.shopsphere.products.dto.ProductBrandResponse;
import com.siva.shopsphere.products.dto.ProductCategoryResponse;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(CartController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void unauthenticatedGetCartIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedGetCartReturnsOk() throws Exception {
        authenticate("customer-token", "customer@example.com", "CUSTOMER");
        when(cartService.getCart()).thenReturn(cartResponse());

        mockMvc.perform(get("/api/v1/cart").header("Authorization", "Bearer customer-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItemCount").value(1));
    }

    @Test
    void authenticatedAdminCanAccessOwnCart() throws Exception {
        authenticate("admin-token", "admin@example.com", "ADMIN");
        when(cartService.getCart()).thenReturn(cartResponse());

        mockMvc.perform(get("/api/v1/cart").header("Authorization", "Bearer admin-token"))
            .andExpect(status().isOk());
    }

    @Test
    void addItemReturnsCreated() throws Exception {
        authenticate("customer-token", "customer@example.com", "CUSTOMER");
        when(cartService.addItem(any())).thenReturn(cartResponse());

        mockMvc.perform(post("/api/v1/cart/items")
                .header("Authorization", "Bearer customer-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":\"cccccccc-cccc-cccc-cccc-cccccccccccc\",\"quantity\":1}"))
            .andExpect(status().isCreated());
    }

    @Test
    void updateItemReturnsOk() throws Exception {
        authenticate("customer-token", "customer@example.com", "CUSTOMER");
        when(cartService.updateItemQuantity(any(), any())).thenReturn(cartResponse());

        mockMvc.perform(put("/api/v1/cart/items/cccccccc-cccc-cccc-cccc-cccccccccccc")
                .header("Authorization", "Bearer customer-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":2}"))
            .andExpect(status().isOk());
    }

    @Test
    void deleteItemReturnsNoContent() throws Exception {
        authenticate("customer-token", "customer@example.com", "CUSTOMER");

        mockMvc.perform(delete("/api/v1/cart/items/cccccccc-cccc-cccc-cccc-cccccccccccc")
                .header("Authorization", "Bearer customer-token"))
            .andExpect(status().isNoContent());
    }

    @Test
    void clearCartReturnsNoContent() throws Exception {
        authenticate("customer-token", "customer@example.com", "CUSTOMER");

        mockMvc.perform(delete("/api/v1/cart").header("Authorization", "Bearer customer-token"))
            .andExpect(status().isNoContent());
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

    private CartResponse cartResponse() {
        return new CartResponse(
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            List.of(cartItemResponse()),
            1,
            new BigDecimal("9999.00"),
            Instant.now(),
            Instant.now()
        );
    }

    private CartItemResponse cartItemResponse() {
        return new CartItemResponse(
            UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            "MacBook Air M3",
            "https://example.com/macbook.jpg",
            new ProductCategoryResponse(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"), "Electronics", "electronics"),
            new ProductBrandResponse(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"), "Apple", "apple"),
            1,
            new BigDecimal("9999.00"),
            new BigDecimal("9999.00"),
            true
        );
    }
}
