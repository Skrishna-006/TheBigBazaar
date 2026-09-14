package com.siva.shopsphere.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.siva.shopsphere.accounts.entity.OtpPurpose;
import com.siva.shopsphere.accounts.entity.OtpVerification;
import com.siva.shopsphere.accounts.repository.OtpVerificationRepository;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.exception.ServiceUnavailableException;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpVerificationRepository otpRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SmsService smsService;
    
    @Mock
    private OtpDeliveryService emailDeliveryService;

    @InjectMocks
    private OtpService otpService;

    @Captor
    private ArgumentCaptor<OtpVerification> verificationCaptor;

    private OtpVerification validVerification;

    @BeforeEach
    void setUp() {
        validVerification = new OtpVerification();
        validVerification.setId(UUID.randomUUID());
        validVerification.setPhoneNumber("user@example.com");
        validVerification.setOtpHash("hashedOtp");
        validVerification.setPurpose(OtpPurpose.REGISTRATION);
        validVerification.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));
    }

    @Test
    void sendOtpGeneratesNumericOtpAndHashesIt() {
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("hashed123456");

        otpService.sendOtp("9876543210", OtpPurpose.REGISTRATION);

        verify(otpRepository).save(verificationCaptor.capture());
        OtpVerification saved = verificationCaptor.getValue();

        assertThat(saved.getPhoneNumber()).isEqualTo("9876543210");
        assertThat(saved.getOtpHash()).isEqualTo("hashed123456");
        assertThat(saved.getExpiresAt()).isAfter(Instant.now().plus(4, ChronoUnit.MINUTES));
    }

    @Test
    void sendOtpToEmailUsesEmailDeliveryService() {
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("hashedOtp");

        otpService.sendOtp("user@example.com", OtpPurpose.PASSWORD_RESET);

        verify(emailDeliveryService).deliverOtp(eq("user@example.com"), any(String.class));
        verify(smsService, never()).sendSms(any(), any());
    }

    @Test
    void emailProviderFailurePropagatesException() {
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("hashedOtp");
        doThrow(new ServiceUnavailableException("Unable to send OTP via email at this time. Please try again later."))
            .when(emailDeliveryService).deliverOtp(any(), any());

        assertThatThrownBy(() -> otpService.sendOtp("user@example.com", OtpPurpose.REGISTRATION))
            .isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void verifyOtpSucceedsWithValidOtp() {
        when(otpRepository.findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc("user@example.com", OtpPurpose.REGISTRATION))
            .thenReturn(Optional.of(validVerification));
        when(passwordEncoder.matches("123456", "hashedOtp")).thenReturn(true);
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("tokenHash");

        String token = otpService.verifyOtp("user@example.com", "123456", OtpPurpose.REGISTRATION);

        assertThat(token).isNotBlank();
        assertThat(validVerification.getConsumedAt()).isNotNull();
        assertThat(validVerification.getResetTokenHash()).isEqualTo("tokenHash");
        verify(otpRepository).save(validVerification);
    }

    @Test
    void verifyOtpFailsWithInvalidOtp() {
        when(otpRepository.findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc("user@example.com", OtpPurpose.REGISTRATION))
            .thenReturn(Optional.of(validVerification));
        when(passwordEncoder.matches("wrong", "hashedOtp")).thenReturn(false);

        assertThatThrownBy(() -> otpService.verifyOtp("user@example.com", "wrong", OtpPurpose.REGISTRATION))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Invalid OTP");

        assertThat(validVerification.getAttemptCount()).isEqualTo(1);
        verify(otpRepository).save(validVerification);
    }

    @Test
    void verifyOtpFailsWhenExpired() {
        validVerification.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        when(otpRepository.findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc("user@example.com", OtpPurpose.REGISTRATION))
            .thenReturn(Optional.of(validVerification));

        assertThatThrownBy(() -> otpService.verifyOtp("user@example.com", "123456", OtpPurpose.REGISTRATION))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("expired");
    }

    @Test
    void verifyOtpFailsWhenMaximumAttemptsReached() {
        validVerification.setAttemptCount(5);
        when(otpRepository.findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc("user@example.com", OtpPurpose.REGISTRATION))
            .thenReturn(Optional.of(validVerification));

        assertThatThrownBy(() -> otpService.verifyOtp("user@example.com", "123456", OtpPurpose.REGISTRATION))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Maximum attempts reached");

        assertThat(validVerification.getConsumedAt()).isNotNull(); // Automatically invalidated
    }

    @Test
    void verifyOtpFailsWhenAlreadyConsumed() {
        validVerification.setConsumedAt(Instant.now());
        when(otpRepository.findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc("user@example.com", OtpPurpose.REGISTRATION))
            .thenReturn(Optional.of(validVerification));

        assertThatThrownBy(() -> otpService.verifyOtp("user@example.com", "123456", OtpPurpose.REGISTRATION))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("already been used");
    }
}
