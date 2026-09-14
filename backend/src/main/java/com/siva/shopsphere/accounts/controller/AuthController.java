package com.siva.shopsphere.accounts.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.siva.shopsphere.accounts.dto.AuthResponse;
import com.siva.shopsphere.accounts.dto.LoginRequest;
import com.siva.shopsphere.accounts.dto.RefreshRequest;
import com.siva.shopsphere.accounts.dto.UserResponse;
import com.siva.shopsphere.accounts.dto.SendOtpRequest;
import com.siva.shopsphere.accounts.dto.VerifyOtpRequest;
import com.siva.shopsphere.accounts.dto.VerifyOtpResponse;
import com.siva.shopsphere.accounts.dto.CompleteRegistrationRequest;
import com.siva.shopsphere.accounts.dto.CompletePasswordResetRequest;
import com.siva.shopsphere.accounts.service.AuthService;
import com.siva.shopsphere.accounts.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register/send-otp")
    public ResponseEntity<Void> sendRegistrationOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendRegistrationOtp(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyRegistrationOtp(request));
    }

    @PostMapping("/register/complete")
    public ResponseEntity<AuthResponse> completeRegistration(@Valid @RequestBody CompleteRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.completeRegistration(request));
    }

    @PostMapping("/password-reset/send-otp")
    public ResponseEntity<Void> sendPasswordResetOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendPasswordResetOtp(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyPasswordResetOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyPasswordResetOtp(request));
    }

    @PostMapping("/password-reset/complete")
    public ResponseEntity<Void> completePasswordReset(@Valid @RequestBody CompletePasswordResetRequest request) {
        authService.completePasswordReset(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }
}
