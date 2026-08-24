package com.siva.shopsphere.accounts.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserResponse user
) {
}
