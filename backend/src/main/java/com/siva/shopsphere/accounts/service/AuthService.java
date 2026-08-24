package com.siva.shopsphere.accounts.service;

import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.accounts.dto.AuthResponse;
import com.siva.shopsphere.accounts.dto.LoginRequest;
import com.siva.shopsphere.accounts.dto.RegisterRequest;
import com.siva.shopsphere.accounts.dto.UserResponse;
import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.accounts.mapper.UserMapper;
import com.siva.shopsphere.accounts.repository.UserRepository;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.UnauthorizedException;
import com.siva.shopsphere.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhoneNumber(request.phoneNumber() == null ? null : request.phoneNumber().trim());
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user.setEmailVerified(false);
        user.setAvatarUrl(null);

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, request.password())
        );
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!user.isActive()) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        String email = jwtService.extractSubject(refreshToken);
        User user = userRepository.findByEmail(normalizeEmail(email))
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (!user.isActive()) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        String accessToken = jwtService.generateAccessToken(user);
        return new AuthResponse(accessToken, refreshToken, "Bearer", jwtService.getAccessTokenExpirationSeconds(), UserMapper.toResponse(user));
    }

    public UserResponse currentUser(User user) {
        return UserMapper.toResponse(user);
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

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
