package com.siva.shopsphere.accounts.dto;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Email;

public record SendOtpRequest(
    @NotBlank @Email(message = "Invalid email format") String email
) {}
