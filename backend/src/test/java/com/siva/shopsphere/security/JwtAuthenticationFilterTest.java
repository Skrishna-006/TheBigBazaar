package com.siva.shopsphere.security;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

class JwtAuthenticationFilterTest {

    @Test
    void validBearerTokenPopulatesSecurityContext() throws Exception {
        JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
        CustomUserDetailsService userDetailsService = org.mockito.Mockito.mock(CustomUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            "customer@example.com",
            "hash",
            List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        when(jwtService.isAccessTokenValid("token")).thenReturn(true);
        when(jwtService.extractSubject("token")).thenReturn("customer@example.com");
        when(userDetailsService.loadUserByUsername("customer@example.com")).thenReturn(userDetails);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {};

        filter.doFilter(request, response, filterChain);

        verify(userDetailsService).loadUserByUsername("customer@example.com");
    }

    @Test
    void missingBearerHeaderDoesNothing() throws Exception {
        JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
        CustomUserDetailsService userDetailsService = org.mockito.Mockito.mock(CustomUserDetailsService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (req, res) -> {});
    }
}
