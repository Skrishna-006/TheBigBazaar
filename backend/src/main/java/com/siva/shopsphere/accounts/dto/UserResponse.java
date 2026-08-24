package com.siva.shopsphere.accounts.dto;

import java.time.Instant;
import java.util.UUID;

import com.siva.shopsphere.accounts.entity.UserRole;

public record UserResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    UserRole role,
    String avatarUrl,
    boolean emailVerified,
    Instant createdAt,
    Instant updatedAt
) {
}
