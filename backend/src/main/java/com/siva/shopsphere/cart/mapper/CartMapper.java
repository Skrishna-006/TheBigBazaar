package com.siva.shopsphere.cart.mapper;

import java.math.BigDecimal;
import java.util.List;

import com.siva.shopsphere.cart.dto.CartItemResponse;
import com.siva.shopsphere.cart.dto.CartResponse;
import com.siva.shopsphere.cart.entity.Cart;
import com.siva.shopsphere.cart.entity.CartItem;

public final class CartMapper {

    private CartMapper() {
    }

    public static CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
            .map(CartMapper::toResponse)
            .toList();

        int totalItemCount = items.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal subtotal = items.stream()
            .map(CartItemResponse::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, totalItemCount, subtotal, cart.getCreatedAt(), cart.getUpdatedAt());
    }

    public static CartItemResponse toResponse(CartItem item) {
        return new CartItemResponse(
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getProduct().getImageUrl(),
            new com.siva.shopsphere.products.dto.ProductCategoryResponse(
                item.getProduct().getCategory().getId(),
                item.getProduct().getCategory().getName(),
                item.getProduct().getCategory().getSlug()
            ),
            new com.siva.shopsphere.products.dto.ProductBrandResponse(
                item.getProduct().getBrand().getId(),
                item.getProduct().getBrand().getName(),
                item.getProduct().getBrand().getSlug()
            ),
            item.getQuantity(),
            item.getProduct().getPrice(),
            item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
            isAvailable(item)
        );
    }

    private static boolean isAvailable(CartItem item) {
        return item.getProduct().isActive();
    }
}
