package com.siva.shopsphere.products.dto;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
    UUID id,
    String name,
    String slug,
    String description,
    String imageUrl,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}
