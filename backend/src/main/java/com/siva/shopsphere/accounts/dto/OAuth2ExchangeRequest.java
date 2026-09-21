package com.siva.shopsphere.accounts.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuth2ExchangeRequest(
    @NotBlank(message = "Authorization code is required")
    String code
) {}
