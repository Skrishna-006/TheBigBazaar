package com.siva.shopsphere.accounts.mapper;

import com.siva.shopsphere.accounts.dto.UserResponse;
import com.siva.shopsphere.accounts.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getRole(),
            user.getAvatarUrl(),
            user.isEmailVerified(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
