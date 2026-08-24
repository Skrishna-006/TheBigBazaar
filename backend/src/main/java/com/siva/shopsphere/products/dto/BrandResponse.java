package com.siva.shopsphere.products.dto;

import java.time.Instant;
import java.util.UUID;

public record BrandResponse(
    UUID id,
    String name,
    String slug,
    String description,
    String logoUrl,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}
