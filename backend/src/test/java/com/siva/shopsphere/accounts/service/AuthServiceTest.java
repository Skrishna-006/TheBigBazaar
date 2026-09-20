package com.siva.shopsphere.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.siva.shopsphere.accounts.dto.AuthResponse;
import com.siva.shopsphere.accounts.dto.LoginRequest;
import com.siva.shopsphere.accounts.dto.RegisterRequest;
import com.siva.shopsphere.accounts.dto.PasswordResetRequest;
import com.siva.shopsphere.accounts.dto.CompletePasswordResetRequest;
import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.accounts.repository.UserRepository;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.UnauthorizedException;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordResetEmailService emailService;

    private final String frontendUrl = "http://localhost:5173";

    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService, emailService, frontendUrl);
        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setPasswordHash("$2a$10$hash");
        sampleUser.setFirstName("Test");
        sampleUser.setLastName("Customer");
        sampleUser.setEmail("user@example.com");
        sampleUser.setRole(UserRole.CUSTOMER);
        sampleUser.setActive(true);
        sampleUser.setEmailVerified(false);
    }

    @Test
    void registerCreatesCustomerWithHashedPassword() {
        RegisterRequest request = new RegisterRequest("user@example.com", "Password@123", "Test", "Customer", "1234567890");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(sampleUser.getId());
            return user;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.user().email()).isEqualTo("user@example.com");
        assertThat(response.user().role()).isEqualTo(UserRole.CUSTOMER);
        verify(passwordEncoder).encode("Password@123");
    }

    @Test
    void duplicateEmailIsRejectedDuringRegistration() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("user@example.com", "Password@123", "Test", "Customer", "1234567890")))
            .isInstanceOf(ConflictException.class);
    }
    
    @Test
    void requestPasswordResetGeneratesTokenAndHashesIt() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode(any(String.class))).thenReturn("hashedToken");
        
        authService.requestPasswordReset(new PasswordResetRequest("user@example.com"));
        
        verify(userRepository).save(any(User.class));
        verify(emailService).sendPasswordResetEmail(eq("user@example.com"), any(String.class));
        assertThat(sampleUser.getResetTokenHash()).isEqualTo("hashedToken");
        assertThat(sampleUser.getResetTokenExpiry()).isNotNull();
    }

    @Test
    void requestPasswordResetIgnoresNonExistingEmail() {
        when(userRepository.findByEmail("nonexisting@example.com")).thenReturn(Optional.empty());

        authService.requestPasswordReset(new PasswordResetRequest("nonexisting@example.com"));

        verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
        verify(emailService, org.mockito.Mockito.never()).sendPasswordResetEmail(any(), any());
    }
    
    @Test
    void resetPasswordSucceedsWithValidToken() {
        sampleUser.setResetTokenHash("hashedToken");
        sampleUser.setResetTokenExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("rawToken", "hashedToken")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("newHashedPassword");
        
        authService.resetPassword(new CompletePasswordResetRequest("user@example.com", "rawToken", "NewPassword@123"));
        
        verify(userRepository).save(sampleUser);
        assertThat(sampleUser.getPasswordHash()).isEqualTo("newHashedPassword");
        assertThat(sampleUser.getResetTokenHash()).isNull();
        assertThat(sampleUser.getResetTokenExpiry()).isNull();
    }
    
    @Test
    void resetPasswordFailsWhenTokenExpired() {
        sampleUser.setResetTokenHash("hashedToken");
        sampleUser.setResetTokenExpiry(Instant.now().minus(10, ChronoUnit.MINUTES));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        
        assertThatThrownBy(() -> authService.resetPassword(new CompletePasswordResetRequest("user@example.com", "rawToken", "NewPassword@123")))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("expired");
    }

    @Test
    void resetPasswordFailsWithInvalidToken() {
        sampleUser.setResetTokenHash("hashedToken");
        sampleUser.setResetTokenExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("invalidRawToken", "hashedToken")).thenReturn(false);

        assertThatThrownBy(() -> authService.resetPassword(new CompletePasswordResetRequest("user@example.com", "invalidRawToken", "NewPassword@123")))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Invalid reset token or email");
    }

    @Test
    void resetPasswordFailsWhenTokenReused() {
        // Token reused means resetTokenHash is null
        sampleUser.setResetTokenHash(null);
        sampleUser.setResetTokenExpiry(null);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> authService.resetPassword(new CompletePasswordResetRequest("user@example.com", "rawToken", "NewPassword@123")))
            .isInstanceOf(BadRequestException.class)
            .hasMessage("Invalid reset token or email");
    }

    @Test
    void loginUsesAuthenticationManagerAndReturnsTokens() {
        when(jwtService.generateAccessToken(sampleUser)).thenReturn("access");
        when(jwtService.generateRefreshToken(sampleUser)).thenReturn("refresh");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));

        AuthResponse response = authService.login(new LoginRequest("user@example.com", "Password@123"));

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("user@example.com", "Password@123"));
        assertThat(response.accessToken()).isEqualTo("access");
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorizedWithoutLeakingDetails() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "WrongPassword@123")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Invalid email or password.");
    }

    @Test
    void refreshRequiresValidRefreshToken() {
        when(jwtService.isRefreshTokenValid("refresh")).thenReturn(true);
        when(jwtService.extractSubject("refresh")).thenReturn("user@example.com");
        when(jwtService.generateAccessToken(sampleUser)).thenReturn("new-access");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(userRepository.findByPhoneNumber("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));

        AuthResponse response = authService.refresh("refresh");

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
    }
}
