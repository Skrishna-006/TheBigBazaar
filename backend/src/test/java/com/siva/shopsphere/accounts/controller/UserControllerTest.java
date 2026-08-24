package com.siva.shopsphere.accounts.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

import com.siva.shopsphere.accounts.dto.UserResponse;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.accounts.service.UserService;
import com.siva.shopsphere.config.SecurityConfig;
import com.siva.shopsphere.config.WebConfig;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void meWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void meWithAuthenticationReturnsProfile() throws Exception {
        when(jwtService.isAccessTokenValid("token")).thenReturn(true);
        when(jwtService.extractSubject("token")).thenReturn("customer@example.com");
        when(customUserDetailsService.loadUserByUsername("customer@example.com")).thenReturn(
            org.springframework.security.core.userdetails.User.withUsername("customer@example.com")
                .password("hash")
                .roles("CUSTOMER")
                .build()
        );
        when(userService.getCurrentUser()).thenReturn(userResponse());

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("customer@example.com"));
    }

    @Test
    void updateProfileReturnsOk() throws Exception {
        when(jwtService.isAccessTokenValid("token")).thenReturn(true);
        when(jwtService.extractSubject("token")).thenReturn("customer@example.com");
        when(customUserDetailsService.loadUserByUsername("customer@example.com")).thenReturn(
            org.springframework.security.core.userdetails.User.withUsername("customer@example.com")
                .password("hash")
                .roles("CUSTOMER")
                .build()
        );
        when(userService.updateCurrentUser(any())).thenReturn(userResponse());

        mockMvc.perform(put("/api/v1/users/me")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"firstName":"Siva","lastName":"Krishna","phoneNumber":"9876543210","avatarUrl":"https://example.com/avatar.jpg"}
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Test"));
    }

    @Test
    void changePasswordReturnsNoContent() throws Exception {
        when(jwtService.isAccessTokenValid("token")).thenReturn(true);
        when(jwtService.extractSubject("token")).thenReturn("customer@example.com");
        when(customUserDetailsService.loadUserByUsername("customer@example.com")).thenReturn(
            org.springframework.security.core.userdetails.User.withUsername("customer@example.com")
                .password("hash")
                .roles("CUSTOMER")
                .build()
        );

        mockMvc.perform(put("/api/v1/users/me/password")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"currentPassword":"OldPassword@123","newPassword":"NewPassword@123","confirmPassword":"NewPassword@123"}
                """))
            .andExpect(status().isNoContent());
    }

    private UserResponse userResponse() {
        return new UserResponse(UUID.randomUUID(), "customer@example.com", "Test", "Customer", "9876543210", UserRole.CUSTOMER, null, false, Instant.now(), Instant.now());
    }
}
