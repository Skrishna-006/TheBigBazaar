package com.siva.shopsphere.accounts.service;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.accounts.dto.AuthResponse;
import com.siva.shopsphere.accounts.dto.LoginRequest;
import com.siva.shopsphere.accounts.dto.RefreshRequest;
import com.siva.shopsphere.accounts.dto.SendOtpRequest;
import com.siva.shopsphere.accounts.dto.VerifyOtpRequest;
import com.siva.shopsphere.accounts.dto.VerifyOtpResponse;
import com.siva.shopsphere.accounts.dto.CompleteRegistrationRequest;
import com.siva.shopsphere.accounts.dto.CompletePasswordResetRequest;
import com.siva.shopsphere.accounts.entity.OtpPurpose;
import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.accounts.mapper.UserMapper;
import com.siva.shopsphere.accounts.repository.UserRepository;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.UnauthorizedException;
import com.siva.shopsphere.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    @Transactional
    public void sendRegistrationOtp(SendOtpRequest request) {
        String email = request.email().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }
        otpService.sendOtp(email, OtpPurpose.REGISTRATION);
    }

    @Transactional
    public VerifyOtpResponse verifyRegistrationOtp(VerifyOtpRequest request) {
        String email = request.email().toLowerCase().trim();
        String token = otpService.verifyOtp(email, request.otp(), OtpPurpose.REGISTRATION);
        return new VerifyOtpResponse(token);
    }

    @Transactional
    public AuthResponse completeRegistration(CompleteRegistrationRequest request) {
        String email = request.email().toLowerCase().trim();
        otpService.validateToken(email, request.token(), OtpPurpose.REGISTRATION);
        
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user.setEmailVerified(true);
        user.setAvatarUrl(null);

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public void sendPasswordResetOtp(SendOtpRequest request) {
        String email = request.email().toLowerCase().trim();
        if (userRepository.existsByEmail(email)) {
            otpService.sendOtp(email, OtpPurpose.PASSWORD_RESET);
        }
    }

    @Transactional
    public VerifyOtpResponse verifyPasswordResetOtp(VerifyOtpRequest request) {
        String email = request.email().toLowerCase().trim();
        String token = otpService.verifyOtp(email, request.otp(), OtpPurpose.PASSWORD_RESET);
        return new VerifyOtpResponse(token);
    }

    @Transactional
    public void completePasswordReset(CompletePasswordResetRequest request) {
        String email = request.email().toLowerCase().trim();
        otpService.validateToken(email, request.token(), OtpPurpose.PASSWORD_RESET);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User not found"));
            
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
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
}
