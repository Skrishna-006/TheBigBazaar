package com.siva.shopsphere.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.entity.UserRole;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
        900000,
        604800000
    );

    @Test
    void accessTokenIsValidAndContainsSubjectAndRole() {
        User user = user();

        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isAccessTokenValid(token)).isTrue();
        assertThat(jwtService.extractSubject(token)).isEqualTo("customer@example.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("CUSTOMER");
    }

    @Test
    void refreshTokenIsValid() {
        String token = jwtService.generateRefreshToken(user());
        assertThat(jwtService.isRefreshTokenValid(token)).isTrue();
    }

    @Test
    void malformedTokenIsRejected() {
        assertThat(jwtService.isAccessTokenValid("bad.token.value")).isFalse();
    }

    private User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("customer@example.com");
        user.setPasswordHash("hash");
        user.setFirstName("Test");
        user.setLastName("Customer");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        return user;
    }
}
