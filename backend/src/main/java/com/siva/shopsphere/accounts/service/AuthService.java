package com.siva.shopsphere.accounts.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siva.shopsphere.accounts.dto.AuthResponse;
import com.siva.shopsphere.accounts.dto.LoginRequest;
import com.siva.shopsphere.accounts.dto.RefreshRequest;
import com.siva.shopsphere.accounts.dto.RegisterRequest;
import com.siva.shopsphere.accounts.dto.PasswordResetRequest;
import com.siva.shopsphere.accounts.dto.CompletePasswordResetRequest;
import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.accounts.mapper.UserMapper;
import com.siva.shopsphere.accounts.repository.UserRepository;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.UnauthorizedException;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.security.JwtService;
import com.siva.shopsphere.security.OAuth2CodeService;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordResetEmailService emailService;
    private final OAuth2CodeService oauth2CodeService;
    private final String frontendUrl;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, PasswordResetEmailService emailService, OAuth2CodeService oauth2CodeService, @org.springframework.beans.factory.annotation.Value("${app.frontend-url}") String frontendUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.oauth2CodeService = oauth2CodeService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public AuthResponse exchangeOAuth2Code(String code) {
        java.util.UUID userId = oauth2CodeService.exchangeCode(code);
        if (userId == null) {
            throw new BadRequestException("Invalid or expired authorization code");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));
        return new AuthResponse(
            jwtService.generateAccessToken(user),
            jwtService.generateRefreshToken(user),
            "Bearer",
            jwtService.getAccessTokenExpirationSeconds(),
            UserMapper.toResponse(user)
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase().trim();
        
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber().trim());
        }
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user.setEmailVerified(false); // Using false or true depending on requirements.
        user.setAuthProvider(com.siva.shopsphere.accounts.entity.AuthProvider.LOCAL);
        user.setAvatarUrl(null);

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        String email = request.email().toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (user.getAuthProvider() != com.siva.shopsphere.accounts.entity.AuthProvider.LOCAL) {
                return;
            }
            
            String rawToken = generateRandomToken();
            user.setResetTokenHash(passwordEncoder.encode(rawToken));
            user.setResetTokenExpiry(Instant.now().plus(15, ChronoUnit.MINUTES));
            userRepository.save(user);
            
            try {
                String encodedEmail = java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8.toString());
                String resetLink = frontendUrl + "/reset-password?token=" + rawToken + "&email=" + encodedEmail;
                emailService.sendPasswordResetEmail(email, resetLink);
            } catch (Exception e) {
                logger.error("Error creating reset link", e);
            }
        }
    }

    @Transactional
    public void resetPassword(CompletePasswordResetRequest request) {
        String email = request.email().toLowerCase().trim();
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadRequestException("Invalid reset token or email"));
            
        if (user.getAuthProvider() != com.siva.shopsphere.accounts.entity.AuthProvider.LOCAL) {
            throw new BadRequestException("Invalid reset token or email");
        }
            
        if (user.getResetTokenHash() == null || user.getResetTokenExpiry() == null) {
            throw new BadRequestException("Invalid reset token or email");
        }
        
        if (user.getResetTokenExpiry().isBefore(Instant.now())) {
            throw new BadRequestException("Reset token has expired");
        }
        
        if (!passwordEncoder.matches(request.token(), user.getResetTokenHash())) {
            throw new BadRequestException("Invalid reset token or email");
        }
            
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setResetTokenHash(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.email().toLowerCase().trim();
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, request.password())
            );
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid email or password.");
        }
        
        User user = userRepository.findByEmail(identifier)
            .orElseGet(() -> userRepository.findByPhoneNumber(identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password.")));
                
        if (!user.isActive()) {
            throw new UnauthorizedException("Invalid email or password.");
        }
        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        String identifier = jwtService.extractSubject(refreshToken);
        User user = userRepository.findByPhoneNumber(identifier)
            .orElseGet(() -> userRepository.findByEmail(identifier)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token")));
                
        if (!user.isActive()) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        String accessToken = jwtService.generateAccessToken(user);
        return new AuthResponse(accessToken, refreshToken, "Bearer", jwtService.getAccessTokenExpirationSeconds(), UserMapper.toResponse(user));
    }

    public void logout() {
        // Stateless JWT logout is handled client-side in this phase.
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
            jwtService.generateAccessToken(user),
            jwtService.generateRefreshToken(user),
            "Bearer",
            jwtService.getAccessTokenExpirationSeconds(),
            UserMapper.toResponse(user)
        );
    }
    
    private String generateRandomToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
