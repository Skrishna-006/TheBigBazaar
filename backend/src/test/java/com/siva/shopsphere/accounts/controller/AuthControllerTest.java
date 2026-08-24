package com.siva.shopsphere.accounts.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.siva.shopsphere.accounts.dto.AuthResponse;
import com.siva.shopsphere.accounts.dto.UserResponse;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.accounts.service.AuthService;
import com.siva.shopsphere.accounts.service.UserService;
import com.siva.shopsphere.config.SecurityConfig;
import com.siva.shopsphere.config.WebConfig;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void registerReturnsCreated() throws Exception {
        AuthResponse response = new AuthResponse("access", "refresh", "Bearer", 900, userResponse());
        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"customer@example.com","password":"Password@123","firstName":"Test","lastName":"Customer","phoneNumber":"9876543210"}
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").value("access"))
            .andExpect(jsonPath("$.user.email").value("customer@example.com"));
    }

    @Test
    void loginReturnsOk() throws Exception {
        AuthResponse response = new AuthResponse("access", "refresh", "Bearer", 900, userResponse());
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"customer@example.com","password":"Password@123"}
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    void meReturnsAuthenticatedUser() throws Exception {
        when(jwtService.isAccessTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractSubject("valid-token")).thenReturn("customer@example.com");
        when(customUserDetailsService.loadUserByUsername("customer@example.com")).thenReturn(
            org.springframework.security.core.userdetails.User.withUsername("customer@example.com")
                .password("hash")
                .roles("CUSTOMER")
                .build()
        );
        when(userService.getCurrentUser()).thenReturn(userResponse());

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("customer@example.com"))
            .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void meWithoutJwtIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    private UserResponse userResponse() {
        return new UserResponse(UUID.randomUUID(), "customer@example.com", "Test", "Customer", "9876543210", UserRole.CUSTOMER, null, false, Instant.now(), Instant.now());
    }
}
