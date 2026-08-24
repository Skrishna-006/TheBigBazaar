package com.siva.shopsphere.accounts.dto;

import java.time.Instant;
import java.util.UUID;

public record AddressResponse(
    UUID id,
    String firstName,
    String lastName,
    String phone,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String postalCode,
    String country,
    boolean defaultAddress,
    Instant createdAt,
    Instant updatedAt
) {
}
