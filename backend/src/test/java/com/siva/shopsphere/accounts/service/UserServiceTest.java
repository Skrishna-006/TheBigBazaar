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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.siva.shopsphere.accounts.dto.ChangePasswordRequest;
import com.siva.shopsphere.accounts.dto.UpdateProfileRequest;
import com.siva.shopsphere.accounts.dto.UserResponse;
import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.accounts.repository.UserRepository;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("customer@example.com");
        user.setPasswordHash("$2a$10$oldhash");
        user.setFirstName("Test");
        user.setLastName("Customer");
        user.setPhoneNumber("9876543210");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user.setEmailVerified(false);
        when(currentUserService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void getCurrentUserReturnsCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        assertThat(response.email()).isEqualTo("customer@example.com");
        assertThat(response.role()).isEqualTo(UserRole.CUSTOMER);
    }

    @Test
    void updateProfileUpdatesAllowedFieldsOnly() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateCurrentUser(new UpdateProfileRequest("Siva", "Krishna", "9999999999", "https://example.com/avatar.jpg"));

        assertThat(response.firstName()).isEqualTo("Siva");
        assertThat(user.getFirstName()).isEqualTo("Siva");
        assertThat(user.getLastName()).isEqualTo("Krishna");
        assertThat(user.getPhoneNumber()).isEqualTo("9999999999");
        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");
        assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(user.isActive()).isTrue();
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.getId()).isNotNull();
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() {
        when(passwordEncoder.matches("WrongPassword@123", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(new ChangePasswordRequest("WrongPassword@123", "NewPassword@123", "NewPassword@123")))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void changePasswordRejectsConfirmationMismatch() {
        when(passwordEncoder.matches("OldPassword@123", user.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> userService.changePassword(new ChangePasswordRequest("OldPassword@123", "NewPassword@123", "Mismatch@123")))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void changePasswordHashesNewPasswordAndPersistsIt() {
        when(passwordEncoder.matches("OldPassword@123", user.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches("NewPassword@123", user.getPasswordHash())).thenReturn(false);
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("$2a$10$newhash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(new ChangePasswordRequest("OldPassword@123", "NewPassword@123", "NewPassword@123"));

        verify(passwordEncoder).encode("NewPassword@123");
        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$newhash");
    }

    @Test
    void currentPasswordNoLongerWorksAfterSuccessfulChange() {
        when(passwordEncoder.matches("OldPassword@123", user.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.matches("NewPassword@123", user.getPasswordHash())).thenReturn(false);
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("$2a$10$newhash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(new ChangePasswordRequest("OldPassword@123", "NewPassword@123", "NewPassword@123"));

        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$newhash");
        assertThat(user.getPasswordHash()).doesNotContain("oldhash");
    }
}
