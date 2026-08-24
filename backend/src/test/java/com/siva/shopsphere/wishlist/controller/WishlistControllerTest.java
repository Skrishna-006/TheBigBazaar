package com.siva.shopsphere.wishlist.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.siva.shopsphere.products.dto.ProductBrandResponse;
import com.siva.shopsphere.products.dto.ProductCategoryResponse;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;
import com.siva.shopsphere.wishlist.dto.WishlistItemResponse;
import com.siva.shopsphere.wishlist.dto.WishlistResponse;
import com.siva.shopsphere.wishlist.service.WishlistService;

@WebMvcTest(WishlistController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishlistService wishlistService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void unauthenticatedGetWishlistReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/wishlist"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedGetWishlistReturnsOk() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(wishlistService.getWishlist()).thenReturn(wishlistResponse());

        mockMvc.perform(get("/api/v1/wishlist").header("Authorization", "Bearer token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void addDuplicateReturnsConflict() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(wishlistService.addItem(any())).thenThrow(new com.siva.shopsphere.exception.ConflictException("Product is already in your wishlist"));

        mockMvc.perform(post("/api/v1/wishlist/items")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":\"cccccccc-cccc-cccc-cccc-cccccccccccc\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void removeWishlistItemReturnsNoContent() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");

        mockMvc.perform(delete("/api/v1/wishlist/items/cccccccc-cccc-cccc-cccc-cccccccccccc")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isNoContent());
    }

    @Test
    void checkWishlistStatusReturnsOk() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(wishlistService.isWishlisted(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")))
            .thenReturn(new com.siva.shopsphere.wishlist.dto.WishlistedResponse(true));

        mockMvc.perform(get("/api/v1/wishlist/items/cccccccc-cccc-cccc-cccc-cccccccccccc")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.wishlisted").value(true));
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

    private WishlistResponse wishlistResponse() {
        return new WishlistResponse(List.of(wishlistItemResponse()), 1);
    }

    private WishlistItemResponse wishlistItemResponse() {
        return new WishlistItemResponse(
            UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            "MacBook Air M3",
            "macbook-air-m3",
            "https://example.com/macbook.jpg",
            new BigDecimal("9999.00"),
            new ProductCategoryResponse(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"), "Electronics", "electronics"),
            new ProductBrandResponse(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"), "Apple", "apple"),
            true,
            Instant.now()
        );
    }
}
