package com.siva.shopsphere.reviews.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ReviewSummaryResponse(
    BigDecimal averageRating,
    long totalReviews,
    Map<Integer, Long> ratingDistribution
) {
}
