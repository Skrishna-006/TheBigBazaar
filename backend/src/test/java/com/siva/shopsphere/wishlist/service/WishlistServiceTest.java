package com.siva.shopsphere.wishlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.products.entity.Brand;
import com.siva.shopsphere.products.entity.Category;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.security.CurrentUserService;
import com.siva.shopsphere.wishlist.dto.AddWishlistItemRequest;
import com.siva.shopsphere.wishlist.dto.WishlistResponse;
import com.siva.shopsphere.wishlist.entity.WishlistItem;
import com.siva.shopsphere.wishlist.repository.WishlistItemRepository;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistItemRepository wishlistItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    void emptyWishlistReturnsEmptyResponse() {
        User user = user();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of());

        WishlistResponse response = wishlistService.getWishlist();

        assertThat(response.items()).isEmpty();
        assertThat(response.totalItems()).isZero();
    }

    @Test
    void addProductToWishlist() {
        User user = user();
        Product product = product(true);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(wishlistItemRepository.existsByUserIdAndProductId(user.getId(), product.getId())).thenReturn(false);
        when(wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of(wishlistItem(user, product)));
        when(wishlistItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WishlistResponse response = wishlistService.addItem(new AddWishlistItemRequest(product.getId()));

        assertThat(response.totalItems()).isEqualTo(1);
    }

    @Test
    void duplicateProductRejected() {
        User user = user();
        Product product = product(true);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(wishlistItemRepository.existsByUserIdAndProductId(user.getId(), product.getId())).thenReturn(true);

        assertThrows(ConflictException.class, () -> wishlistService.addItem(new AddWishlistItemRequest(product.getId())));
    }

    @Test
    void inactiveProductRejected() {
        User user = user();
        Product product = product(false);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(ResourceNotFoundException.class, () -> wishlistService.addItem(new AddWishlistItemRequest(product.getId())));
    }

    @Test
    void removeItemDeletesEntry() {
        User user = user();
        Product product = product(true);
        WishlistItem item = wishlistItem(user, product);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistItemRepository.findByUserIdAndProductId(user.getId(), product.getId())).thenReturn(Optional.of(item));

        wishlistService.removeItem(product.getId());

        verify(wishlistItemRepository).delete(item);
    }

    @Test
    void wishlistedCheckReturnsTrueWhenPresent() {
        User user = user();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(wishlistItemRepository.existsByUserIdAndProductId(user.getId(), UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"))).thenReturn(true);

        assertThat(wishlistService.isWishlisted(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")).wishlisted()).isTrue();
    }

    private User user() {
        User user = new User();
        user.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        user.setEmail("customer@example.com");
        return user;
    }

    private Product product(boolean active) {
        Product product = new Product();
        product.setId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
        product.setName("MacBook Air M3");
        product.setSlug("macbook-air-m3");
        product.setSku("MBA-M3-256");
        product.setPrice(new BigDecimal("9999.00"));
        product.setActive(active);
        Category category = new Category();
        category.setId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));
        category.setName("Electronics");
        category.setSlug("electronics");
        Brand brand = new Brand();
        brand.setId(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"));
        brand.setName("Apple");
        brand.setSlug("apple");
        product.setCategory(category);
        product.setBrand(brand);
        return product;
    }

    private WishlistItem wishlistItem(User user, Product product) {
        WishlistItem item = new WishlistItem();
        item.setId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"));
        item.setUser(user);
        item.setProduct(product);
        item.setCreatedAt(Instant.now());
        return item;
    }
}
