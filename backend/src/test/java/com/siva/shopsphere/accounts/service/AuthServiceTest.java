package com.siva.shopsphere.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.accounts.repository.UserRepository;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.UnauthorizedException;
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

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setEmail("customer@example.com");
        sampleUser.setPasswordHash("$2a$10$hash");
        sampleUser.setFirstName("Test");
        sampleUser.setLastName("Customer");
        sampleUser.setPhoneNumber("9876543210");
        sampleUser.setRole(UserRole.CUSTOMER);
        sampleUser.setActive(true);
        sampleUser.setEmailVerified(false);
    }

    @Test
    void registerCreatesCustomerWithHashedPassword() {
        RegisterRequest request = new RegisterRequest("User@Example.com", "Password@123", "Test", "Customer", "9876543210");
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
    void duplicateEmailIsRejected() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("user@example.com", "Password@123", "Test", "Customer", "9876543210")))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void loginUsesAuthenticationManagerAndReturnsTokens() {
        when(jwtService.generateAccessToken(sampleUser)).thenReturn("access");
        when(jwtService.generateRefreshToken(sampleUser)).thenReturn("refresh");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(sampleUser));

        AuthResponse response = authService.login(new LoginRequest("Customer@Example.com", "Password@123"));

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken("customer@example.com", "Password@123"));
        assertThat(response.accessToken()).isEqualTo("access");
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorizedWithoutLeakingDetails() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("customer@example.com", "WrongPassword@123")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Invalid email or password.");
    }

    @Test
    void loginWithUnknownEmailReturnsUnauthorizedWithoutLeakingDetails() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        AuthResponse response = null;
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenAnswer(invocation -> null);

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown@example.com", "Password@123")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Invalid email or password.");
    }

    @Test
    void inactiveUserCannotLogin() {
        sampleUser.setActive(false);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> authService.login(new LoginRequest("customer@example.com", "Password@123")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Invalid email or password.");
    }

    @Test
    void refreshRequiresValidRefreshToken() {
        when(jwtService.isRefreshTokenValid("refresh")).thenReturn(true);
        when(jwtService.extractSubject("refresh")).thenReturn("customer@example.com");
        when(jwtService.generateAccessToken(sampleUser)).thenReturn("new-access");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(userRepository.findByEmail("customer@example.com")).thenReturn(Optional.of(sampleUser));

        AuthResponse response = authService.refresh("refresh");

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
    }
}
