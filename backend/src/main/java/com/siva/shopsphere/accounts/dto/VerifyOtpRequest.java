package com.siva.shopsphere.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.Email;

public record VerifyOtpRequest(
    @NotBlank @Email(message = "Invalid email format") String email,
    @NotBlank @Size(min = 6, max = 6) String otp
) {}
