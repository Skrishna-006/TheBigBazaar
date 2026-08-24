package com.siva.shopsphere.wishlist.mapper;

import com.siva.shopsphere.products.dto.ProductBrandResponse;
import com.siva.shopsphere.products.dto.ProductCategoryResponse;
import com.siva.shopsphere.wishlist.dto.WishlistItemResponse;
import com.siva.shopsphere.wishlist.entity.WishlistItem;

public final class WishlistMapper {

    private WishlistMapper() {
    }

    public static WishlistItemResponse toResponse(WishlistItem item) {
        return new WishlistItemResponse(
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getProduct().getSlug(),
            item.getProduct().getImageUrl(),
            item.getProduct().getPrice(),
            new ProductCategoryResponse(
                item.getProduct().getCategory().getId(),
                item.getProduct().getCategory().getName(),
                item.getProduct().getCategory().getSlug()
            ),
            new ProductBrandResponse(
                item.getProduct().getBrand().getId(),
                item.getProduct().getBrand().getName(),
                item.getProduct().getBrand().getSlug()
            ),
            item.getProduct().isActive(),
            item.getCreatedAt()
        );
    }
}
