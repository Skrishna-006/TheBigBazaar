package com.siva.shopsphere.products.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSellerResponse(
    UUID id,
    String name,
    String logoUrl,
    BigDecimal rating,
    Integer ratingCount,
    Integer followerCount,
    String description
) {
}
