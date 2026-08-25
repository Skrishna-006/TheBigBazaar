package com.siva.shopsphere.orders.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import com.siva.shopsphere.config.SecurityConfig;
import com.siva.shopsphere.config.WebConfig;
import com.siva.shopsphere.orders.controller.OrderController;
import com.siva.shopsphere.orders.service.OrderService;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class OrderAuthorizationTest {

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
    void anonymousCannotCreateOrder() throws Exception {
        mockMvc.perform(post("/api/v1/orders"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotAccessAdminOrders() throws Exception {
        prepareToken("customer-token", "customer@example.com", "CUSTOMER");
        mockMvc.perform(get("/api/v1/admin/orders").header("Authorization", "Bearer customer-token"))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminOrders() throws Exception {
        prepareToken("admin-token", "admin@example.com", "ADMIN");
        when(orderService.getAdminOrders(null)).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/admin/orders").header("Authorization", "Bearer admin-token"))
            .andExpect(status().isOk());
    }

    @Test
    void customerCannotUpdateOrderStatus() throws Exception {
        prepareToken("customer-token", "customer@example.com", "CUSTOMER");
        mockMvc.perform(patch("/api/v1/admin/orders/11111111-1111-1111-1111-111111111111/status")
                .header("Authorization", "Bearer customer-token")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PROCESSING\"}"))
            .andExpect(status().isForbidden());
    }

    private void prepareToken(String token, String email, String role) {
        when(jwtService.isAccessTokenValid(token)).thenReturn(true);
        when(jwtService.extractSubject(token)).thenReturn(email);
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(userDetails(email, role));
    }

    private UserDetails userDetails(String email, String role) {
        return new org.springframework.security.core.userdetails.User(
            email,
            "hash",
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}
