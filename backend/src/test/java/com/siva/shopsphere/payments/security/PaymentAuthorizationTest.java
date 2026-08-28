package com.siva.shopsphere.payments.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.siva.shopsphere.payments.controller.PaymentController;
import com.siva.shopsphere.payments.service.PaymentService;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(PaymentController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class PaymentAuthorizationTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PaymentService paymentService;
    @MockBean private AuthenticationProvider authenticationProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private JwtService jwtService;

    @Test
    void anonymousCannotCreatePayment() throws Exception {
        mockMvc.perform(post("/api/v1/payments/orders/22222222-2222-2222-2222-222222222222")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CARD\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousCannotReadPayment() throws Exception {
        mockMvc.perform(get("/api/v1/payments/33333333-3333-3333-3333-333333333333"))
            .andExpect(status().isUnauthorized());
    }
}
