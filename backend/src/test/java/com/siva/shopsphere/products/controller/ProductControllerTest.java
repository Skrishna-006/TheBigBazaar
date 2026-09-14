package com.siva.shopsphere.products.controller;

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

import com.siva.shopsphere.config.SecurityConfig;
import com.siva.shopsphere.config.WebConfig;
import com.siva.shopsphere.products.dto.BrandResponse;
import com.siva.shopsphere.products.dto.CategoryResponse;
import com.siva.shopsphere.products.dto.ProductResponse;
import com.siva.shopsphere.products.dto.ProductBrandResponse;
import com.siva.shopsphere.products.dto.ProductCategoryResponse;
import com.siva.shopsphere.products.service.ProductService;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void publicListReturnsOk() throws Exception {
        when(productService.getActiveProducts(null, null)).thenReturn(List.of(productResponse()));

        mockMvc.perform(get("/api/v1/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].slug").value("macbook-air-m3"));
    }

    @Test
    void publicGetByIdReturnsOk() throws Exception {
        when(productService.getProductById(UUID.fromString("11111111-1111-1111-1111-111111111111"))).thenReturn(productResponse());

        mockMvc.perform(get("/api/v1/products/11111111-1111-1111-1111-111111111111"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sku").value("MBA-M3-256"));
    }

    @Test
    void unauthenticatedCreateIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCreateReturnsCreated() throws Exception {
        authenticate("admin-token", "admin@example.com", "ADMIN");
        when(productService.createProduct(any())).thenReturn(productResponse());

        mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.slug").value("macbook-air-m3"));
    }

    @Test
    void customerCreateIsForbidden() throws Exception {
        authenticate("customer-token", "customer@example.com", "CUSTOMER");

        mockMvc.perform(post("/api/v1/products")
                .header("Authorization", "Bearer customer-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson()))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminUpdateReturnsOk() throws Exception {
        authenticate("admin-token", "admin@example.com", "ADMIN");
        when(productService.updateProduct(any(), any())).thenReturn(productResponse());

        mockMvc.perform(put("/api/v1/products/11111111-1111-1111-1111-111111111111")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"MacBook Air M3 Pro","description":"Updated","price":109999.00,"categoryId":"22222222-2222-2222-2222-222222222222","brandId":"33333333-3333-3333-3333-333333333333","imageUrl":"https://example.com/macbook-pro.jpg","active":true}
                """))
            .andExpect(status().isOk());
    }

    @Test
    void adminDeleteReturnsNoContent() throws Exception {
        authenticate("admin-token", "admin@example.com", "ADMIN");

        mockMvc.perform(delete("/api/v1/products/11111111-1111-1111-1111-111111111111")
                .header("Authorization", "Bearer admin-token"))
            .andExpect(status().isNoContent());
    }

    @Test
    void categoryFilterWorks() throws Exception {
        when(productService.getActiveProducts(UUID.fromString("22222222-2222-2222-2222-222222222222"), null)).thenReturn(List.of(productResponse()));

        mockMvc.perform(get("/api/v1/products").param("categoryId", "22222222-2222-2222-2222-222222222222"))
            .andExpect(status().isOk());
    }

    @Test
    void brandFilterWorks() throws Exception {
        when(productService.getActiveProducts(null, UUID.fromString("33333333-3333-3333-3333-333333333333"))).thenReturn(List.of(productResponse()));

        mockMvc.perform(get("/api/v1/products").param("brandId", "33333333-3333-3333-3333-333333333333"))
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

    private String productJson() {
        return """
            {"name":"MacBook Air M3","sku":"MBA-M3-256","description":"Apple MacBook Air with M3 chip","price":99999.00,"categoryId":"22222222-2222-2222-2222-222222222222","brandId":"33333333-3333-3333-3333-333333333333","imageUrl":"https://example.com/macbook.jpg"}
        """;
    }

    private ProductResponse productResponse() {
        return new ProductResponse(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "MacBook Air M3",
            "macbook-air-m3",
            "MBA-M3-256",
            "Apple MacBook Air with M3 chip",
            new BigDecimal("99999.00"),
            null,
            BigDecimal.ZERO,
            5,
            null,
            0,
            new ProductCategoryResponse(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Electronics", "electronics"),
            new ProductBrandResponse(UUID.fromString("33333333-3333-3333-3333-333333333333"), "Apple", "apple"),
            null, // seller
            java.util.Collections.emptyList(), // highlights
            java.util.Collections.emptyMap(), // specifications
            "https://example.com/macbook.jpg",
            true,
            Instant.now(),
            Instant.now()
        );
    }
}
