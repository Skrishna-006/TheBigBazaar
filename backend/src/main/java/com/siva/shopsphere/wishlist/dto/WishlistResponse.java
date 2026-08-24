package com.siva.shopsphere.wishlist.dto;

import java.util.List;

public record WishlistResponse(
    List<WishlistItemResponse> items,
    int totalItems
) {
}
