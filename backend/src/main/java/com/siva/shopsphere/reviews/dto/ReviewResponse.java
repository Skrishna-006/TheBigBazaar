package com.siva.shopsphere.reviews.dto;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID productId,
    Integer rating,
    String title,
    String reviewText,
    String reviewerName,
    boolean verifiedPurchase,
    Integer helpfulCount,
    Instant createdAt
) {
}
