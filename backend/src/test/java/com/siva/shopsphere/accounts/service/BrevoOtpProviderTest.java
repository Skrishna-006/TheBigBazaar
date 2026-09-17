package com.siva.shopsphere.accounts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.siva.shopsphere.exception.ServiceUnavailableException;

import java.util.List;
import java.util.Map;

public class BrevoOtpProviderTest {

    @Mock
    private RestTemplate restTemplateMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInitMissingApiKey() {
        BrevoOtpProvider provider = new BrevoOtpProvider("", "test@test.com", "Test Sender");
        Exception ex = assertThrows(IllegalStateException.class, () -> provider.init());
        assertTrue(ex.getMessage().contains("Brevo API key is not configured"));
    }

    @Test
    void testInitMissingFromEmail() {
        BrevoOtpProvider provider = new BrevoOtpProvider("apikey", "", "Test Sender");
        Exception ex = assertThrows(IllegalStateException.class, () -> provider.init());
        assertTrue(ex.getMessage().contains("Brevo from-email is not configured"));
    }

    @Test
    void testInitSuccess() {
        BrevoOtpProvider provider = new BrevoOtpProvider("apikey", "test@test.com", "Test Sender");
        assertDoesNotThrow(() -> provider.init());
    }

    @Test
    void testDeliverOtpSuccess() {
        BrevoOtpProvider provider = new BrevoOtpProvider("apikey", "test@test.com", "Test Sender");
        provider.init();
        provider.setRestTemplate(restTemplateMock);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{}", HttpStatus.CREATED);
        when(restTemplateMock.exchange(
                eq("https://api.brevo.com/v3/smtp/email"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        assertDoesNotThrow(() -> provider.deliverOtp("user@example.com", "123456"));
        
        verify(restTemplateMock, times(1)).exchange(
                eq("https://api.brevo.com/v3/smtp/email"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        );
        
        // Assert that the response status is treated as successful by verifying no exceptions were thrown
        // and exchange was called with the correct parameters.
    }

    @Test
    @SuppressWarnings("unchecked")
    void testDeliverOtpPayloadVerification() {
        BrevoOtpProvider provider = new BrevoOtpProvider("apikey", "test@test.com", "Test Sender");
        provider.init();
        provider.setRestTemplate(restTemplateMock);

        ResponseEntity<String> responseEntity = new ResponseEntity<>("{}", HttpStatus.OK);
        when(restTemplateMock.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        provider.deliverOtp("user@example.com", "123456");

        verify(restTemplateMock).exchange(
                anyString(),
                any(HttpMethod.class),
                argThat(entity -> {
                    HttpEntity<Map<String, Object>> httpEntity = (HttpEntity<Map<String, Object>>) entity;
                    // Check headers
                    String apiKeyHeader = httpEntity.getHeaders().getFirst("api-key");
                    if (!"apikey".equals(apiKeyHeader)) return false;

                    // Check body
                    Map<String, Object> body = httpEntity.getBody();
                    if (body == null) return false;
                    
                    Map<String, Object> sender = (Map<String, Object>) body.get("sender");
                    if (!"test@test.com".equals(sender.get("email"))) return false;
                    if (!"Test Sender".equals(sender.get("name"))) return false;

                    List<Map<String, Object>> to = (List<Map<String, Object>>) body.get("to");
                    if (!"user@example.com".equals(to.get(0).get("email"))) return false;

                    return true;
                }),
                eq(String.class)
        );
    }

    @Test
    void testDeliverOtpRestClientExceptionThrowsServiceUnavailableException() {
        BrevoOtpProvider provider = new BrevoOtpProvider("apikey", "test@test.com", "Test Sender");
        provider.init();
        provider.setRestTemplate(restTemplateMock);

        when(restTemplateMock.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RestClientException("API Error"));

        Exception ex = assertThrows(ServiceUnavailableException.class, () -> provider.deliverOtp("user@example.com", "123456"));
        assertEquals("Unable to send OTP via email at this time. Please try again later.", ex.getMessage());
    }
}
