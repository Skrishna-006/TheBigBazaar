package com.siva.shopsphere.products.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.siva.shopsphere.products.dto.CategoryResponse;
import com.siva.shopsphere.products.service.CategoryService;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void publicListReturnsOkWithoutJwt() throws Exception {
        when(categoryService.getActiveCategories()).thenReturn(List.of(categoryResponse()));

        mockMvc.perform(get("/api/v1/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].slug").value("electronics"));
    }

    @Test
    void unauthenticatedCreateIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Electronics\",\"description\":\"Devices\",\"imageUrl\":\"https://example.com/electronics.jpg\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCreateReturnsCreated() throws Exception {
        authenticateAdmin("admin-token");
        when(categoryService.createCategory(any())).thenReturn(categoryResponse());

        mockMvc.perform(post("/api/v1/categories")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Electronics\",\"description\":\"Devices\",\"imageUrl\":\"https://example.com/electronics.jpg\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.slug").value("electronics"));
    }

    @Test
    void customerCreateIsForbidden() throws Exception {
        authenticate("customer-token", "customer@example.com", "CUSTOMER");

        mockMvc.perform(post("/api/v1/categories")
                .header("Authorization", "Bearer customer-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Electronics\",\"description\":\"Devices\",\"imageUrl\":\"https://example.com/electronics.jpg\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminUpdateReturnsOk() throws Exception {
        authenticateAdmin("admin-token");
        when(categoryService.updateCategory(any(), any())).thenReturn(categoryResponse());

        mockMvc.perform(put("/api/v1/categories/11111111-1111-1111-1111-111111111111")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Electronics\",\"description\":\"Devices\",\"imageUrl\":\"https://example.com/electronics.jpg\",\"active\":true}"))
            .andExpect(status().isOk());
    }

    @Test
    void adminDeleteReturnsNoContent() throws Exception {
        authenticateAdmin("admin-token");

        mockMvc.perform(delete("/api/v1/categories/11111111-1111-1111-1111-111111111111")
                .header("Authorization", "Bearer admin-token"))
            .andExpect(status().isNoContent());
    }

    private void authenticateAdmin(String token) {
        authenticate(token, "admin@example.com", "ADMIN");
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

    private CategoryResponse categoryResponse() {
        return new CategoryResponse(UUID.randomUUID(), "Electronics", "electronics", "Devices", "https://example.com/electronics.jpg", true, Instant.now(), Instant.now());
    }
}
