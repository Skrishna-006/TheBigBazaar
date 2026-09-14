package com.siva.shopsphere.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.Email;

public record CompleteRegistrationRequest(
    @NotBlank @Email(message = "Invalid email format") String email,
    @NotBlank String token,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters long") String password
) {}
