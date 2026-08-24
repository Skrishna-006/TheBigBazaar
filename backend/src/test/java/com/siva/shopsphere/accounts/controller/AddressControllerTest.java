package com.siva.shopsphere.accounts.controller;

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

import com.siva.shopsphere.accounts.dto.AddressResponse;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.accounts.service.AddressService;
import com.siva.shopsphere.config.SecurityConfig;
import com.siva.shopsphere.config.WebConfig;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.security.CustomUserDetailsService;
import com.siva.shopsphere.security.JwtAuthenticationFilter;
import com.siva.shopsphere.security.JwtService;

@WebMvcTest(AddressController.class)
@Import({SecurityConfig.class, WebConfig.class, JwtAuthenticationFilter.class})
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @Test
    void listWithoutJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/addresses"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void listWithJwtReturnsOk() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(addressService.getCurrentUserAddresses()).thenReturn(List.of(addressResponse(true)));

        mockMvc.perform(get("/api/v1/addresses").header("Authorization", "Bearer token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].defaultAddress").value(true));
    }

    @Test
    void getOtherUsersAddressReturnsNotFound() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(addressService.getAddress(UUID.fromString("11111111-1111-1111-1111-111111111111")))
            .thenThrow(new ResourceNotFoundException("Address not found"));

        mockMvc.perform(get("/api/v1/addresses/11111111-1111-1111-1111-111111111111")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createAddressReturnsCreated() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(addressService.createAddress(any())).thenReturn(addressResponse(false));

        mockMvc.perform(post("/api/v1/addresses")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"firstName":"Siva","lastName":"Krishna","phone":"9876543210","addressLine1":"Main Road","addressLine2":"Near College","city":"Anantapur","state":"Andhra Pradesh","postalCode":"515001","country":"India","defaultAddress":false}
                """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.country").value("India"));
    }

    @Test
    void updateAddressReturnsOk() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");
        when(addressService.updateAddress(any(), any())).thenReturn(addressResponse(false));

        mockMvc.perform(put("/api/v1/addresses/11111111-1111-1111-1111-111111111111")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"firstName":"Siva","lastName":"Krishna","phone":"9876543210","addressLine1":"Main Road","addressLine2":"Near College","city":"Anantapur","state":"Andhra Pradesh","postalCode":"515001","country":"India","defaultAddress":false}
                """))
            .andExpect(status().isOk());
    }

    @Test
    void deleteAddressReturnsNoContent() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");

        mockMvc.perform(delete("/api/v1/addresses/11111111-1111-1111-1111-111111111111")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isNoContent());
    }

    @Test
    void setDefaultReturnsNoContent() throws Exception {
        authenticate("token", "customer@example.com", "CUSTOMER");

        mockMvc.perform(post("/api/v1/addresses/11111111-1111-1111-1111-111111111111/default")
                .header("Authorization", "Bearer token"))
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

    private AddressResponse addressResponse(boolean defaultAddress) {
        return new AddressResponse(UUID.randomUUID(), "Siva", "Krishna", "9876543210", "Main Road", "Near College", "Anantapur", "Andhra Pradesh", "515001", "India", defaultAddress, Instant.now(), Instant.now());
    }
}
