package com.siva.shopsphere.wishlist.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.siva.shopsphere.wishlist.dto.AddWishlistItemRequest;
import com.siva.shopsphere.wishlist.dto.WishlistResponse;
import com.siva.shopsphere.wishlist.dto.WishlistedResponse;
import com.siva.shopsphere.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/wishlist")
@SecurityRequirement(name = "bearer-jwt")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @Operation(summary = "Get the current user's wishlist")
    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist() {
        return ResponseEntity.ok(wishlistService.getWishlist());
    }

    @Operation(summary = "Add a product to the current user's wishlist")
    @PostMapping("/items")
    public ResponseEntity<WishlistResponse> addItem(@Valid @RequestBody AddWishlistItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistService.addItem(request));
    }

    @Operation(summary = "Remove a product from the current user's wishlist")
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID productId) {
        wishlistService.removeItem(productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check whether a product is in the current user's wishlist")
    @GetMapping("/items/{productId}")
    public ResponseEntity<WishlistedResponse> isWishlisted(@PathVariable UUID productId) {
        return ResponseEntity.ok(wishlistService.isWishlisted(productId));
    }
}
