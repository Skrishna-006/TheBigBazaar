package com.siva.shopsphere.wishlist.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.security.CurrentUserService;
import com.siva.shopsphere.wishlist.dto.AddWishlistItemRequest;
import com.siva.shopsphere.wishlist.dto.WishlistItemResponse;
import com.siva.shopsphere.wishlist.dto.WishlistResponse;
import com.siva.shopsphere.wishlist.dto.WishlistedResponse;
import com.siva.shopsphere.wishlist.entity.WishlistItem;
import com.siva.shopsphere.wishlist.mapper.WishlistMapper;
import com.siva.shopsphere.wishlist.repository.WishlistItemRepository;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public WishlistService(WishlistItemRepository wishlistItemRepository, ProductRepository productRepository, CurrentUserService currentUserService) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public WishlistResponse getWishlist() {
        User user = currentUserService.getCurrentUser();
        List<WishlistItemResponse> items = wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(WishlistMapper::toResponse)
            .toList();
        return new WishlistResponse(items, items.size());
    }

    @Transactional
    public WishlistResponse addItem(AddWishlistItemRequest request) {
        User user = currentUserService.getCurrentUser();
        Product product = loadActiveProduct(request.productId());
        if (wishlistItemRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new ConflictException("Product is already in your wishlist");
        }
        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setProduct(product);
        wishlistItemRepository.save(item);
        return getWishlist();
    }

    @Transactional
    public void removeItem(UUID productId) {
        User user = currentUserService.getCurrentUser();
        wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId)
            .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found"));
        wishlistItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    @Transactional(readOnly = true)
    public WishlistedResponse isWishlisted(UUID productId) {
        User user = currentUserService.getCurrentUser();
        return new WishlistedResponse(wishlistItemRepository.existsByUserIdAndProductId(user.getId(), productId));
    }

    private Product loadActiveProduct(UUID productId) {
        return productRepository.findById(productId)
            .filter(Product::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
}
