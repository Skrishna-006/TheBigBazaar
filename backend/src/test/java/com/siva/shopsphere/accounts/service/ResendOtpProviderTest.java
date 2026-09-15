package com.siva.shopsphere.accounts.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.siva.shopsphere.exception.ServiceUnavailableException;

public class ResendOtpProviderTest {

    @Mock
    private Resend resendMock;

    @Mock
    private Emails emailsMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(resendMock.emails()).thenReturn(emailsMock);
    }

    @Test
    void testInitMissingApiKey() {
        ResendOtpProvider provider = new ResendOtpProvider("", "test@test.com", false);
        Exception ex = assertThrows(IllegalStateException.class, () -> provider.init());
        assertTrue(ex.getMessage().contains("Resend API key is not configured"));
    }

    @Test
    void testInitMissingFromEmail() {
        ResendOtpProvider provider = new ResendOtpProvider("apikey", "", false);
        Exception ex = assertThrows(IllegalStateException.class, () -> provider.init());
        assertTrue(ex.getMessage().contains("Resend from-email is not configured"));
    }

    @Test
    void testInitSuccess() {
        ResendOtpProvider provider = new ResendOtpProvider("apikey", "test@test.com", false);
        assertDoesNotThrow(() -> provider.init());
    }

    @Test
    void testDeliverOtpSuccess() throws ResendException {
        ResendOtpProvider provider = new ResendOtpProvider("apikey", "test@test.com", false);
        provider.init();
        ReflectionTestUtils.setField(provider, "resend", resendMock);

        CreateEmailResponse mockResponse = new CreateEmailResponse();
        ReflectionTestUtils.setField(mockResponse, "id", "email_123");
        when(emailsMock.send(any(CreateEmailOptions.class))).thenReturn(mockResponse);

        assertDoesNotThrow(() -> provider.deliverOtp("user@example.com", "123456"));
        verify(emailsMock, times(1)).send(any(CreateEmailOptions.class));
    }

    @Test
    void testDeliverOtpResendExceptionThrowsServiceUnavailableException() throws ResendException {
        ResendOtpProvider provider = new ResendOtpProvider("apikey", "test@test.com", false);
        provider.init();
        ReflectionTestUtils.setField(provider, "resend", resendMock);

        when(emailsMock.send(any(CreateEmailOptions.class))).thenThrow(new ResendException("API Error"));

        Exception ex = assertThrows(ServiceUnavailableException.class, () -> provider.deliverOtp("user@example.com", "123456"));
        assertEquals("Unable to send OTP via email at this time. Please try again later.", ex.getMessage());
    }
}
