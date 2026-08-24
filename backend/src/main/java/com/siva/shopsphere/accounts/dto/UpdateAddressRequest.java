package com.siva.shopsphere.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateAddressRequest(
    @NotBlank @Size(max = 100) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @NotBlank @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Phone is invalid") String phone,
    @NotBlank @Size(max = 255) String addressLine1,
    @Size(max = 255) String addressLine2,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Size(max = 100) String state,
    @NotBlank @Pattern(regexp = "^[A-Za-z0-9\\-\\s]{3,20}$", message = "Postal code is invalid") String postalCode,
    @NotBlank @Size(max = 100) String country,
    Boolean defaultAddress
) {
}
