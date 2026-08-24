package com.siva.shopsphere.accounts.service;

import com.siva.shopsphere.accounts.dto.ChangePasswordRequest;
import com.siva.shopsphere.accounts.dto.UpdateProfileRequest;
import com.siva.shopsphere.accounts.dto.UserResponse;
import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.mapper.UserMapper;
import com.siva.shopsphere.accounts.repository.UserRepository;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.security.CurrentUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(CurrentUserService currentUserService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return UserMapper.toResponse(currentUserService.getCurrentUser());
    }

    @Transactional
    public UserResponse updateCurrentUser(UpdateProfileRequest request) {
        User user = currentUserService.getCurrentUser();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhoneNumber(request.phoneNumber() == null ? null : request.phoneNumber().trim());
        user.setAvatarUrl(request.avatarUrl() == null ? null : request.avatarUrl().trim());
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUserService.getCurrentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Password confirmation does not match");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from the current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
