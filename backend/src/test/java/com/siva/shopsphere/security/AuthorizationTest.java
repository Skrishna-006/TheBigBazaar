package com.siva.shopsphere.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.siva.shopsphere.accounts.service.AddressService;
import com.siva.shopsphere.config.SecurityConfig;
import com.siva.shopsphere.config.WebConfig;
import com.siva.shopsphere.security.JwtAuthenticationFilter;

@WebMvcTest(controllers = AuthorizationTest.TestRoleController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class, AuthorizationTest.TestRoleController.class})
class AuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AddressService addressService;

    @Test
    void customerCannotAccessAdminEndpoint() throws Exception {
        prepareToken("customer-token", "customer@example.com", "CUSTOMER");
        mockMvc.perform(get("/api/v1/admin-only").header("Authorization", "Bearer customer-token"))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminEndpoint() throws Exception {
        prepareToken("admin-token", "admin@example.com", "ADMIN");
        mockMvc.perform(get("/api/v1/admin-only").header("Authorization", "Bearer admin-token"))
            .andExpect(status().isOk());
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

    @RestController
    @RequestMapping("/api/v1")
    static class TestRoleController {
        @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
        @GetMapping("/admin-only")
        String adminOnly() {
            return "ok";
        }
    }
}
