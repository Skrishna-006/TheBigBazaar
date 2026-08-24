package com.siva.shopsphere.accounts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters long") String password,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Phone number is invalid") String phoneNumber
) {
}
