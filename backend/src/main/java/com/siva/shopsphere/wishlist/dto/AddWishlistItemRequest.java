package com.siva.shopsphere.wishlist.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AddWishlistItemRequest(
    @NotNull(message = "Product is required")
    UUID productId
) {
}
