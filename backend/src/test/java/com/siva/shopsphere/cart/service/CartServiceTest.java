package com.siva.shopsphere.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.cart.dto.AddCartItemRequest;
import com.siva.shopsphere.cart.dto.CartResponse;
import com.siva.shopsphere.cart.dto.UpdateCartItemRequest;
import com.siva.shopsphere.cart.entity.Cart;
import com.siva.shopsphere.cart.entity.CartItem;
import com.siva.shopsphere.cart.repository.CartItemRepository;
import com.siva.shopsphere.cart.repository.CartRepository;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.inventory.service.InventoryService;
import com.siva.shopsphere.products.entity.Brand;
import com.siva.shopsphere.products.entity.Category;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CartService cartService;

    @Test
    void getCartCreatesEmptyCart() {
        User user = user();
        Cart cart = cart(user);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenReturn(cart);
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

        CartResponse response = cartService.getCart();

        assertThat(response.items()).isEmpty();
        assertThat(response.totalItemCount()).isZero();
        assertThat(response.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getCartWithExistingCartAndProductReturnsCartResponse() {
        User user = user();
        Cart cart = cart(user);
        Product product = product();
        CartItem item = cartItem(cart, product, 2);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId())).thenReturn(List.of(item));

        CartResponse response = cartService.getCart();

        assertThat(response.items()).hasSize(1);
        assertThat(response.totalItemCount()).isEqualTo(2);
        assertThat(response.items().get(0).productId()).isEqualTo(product.getId());
        assertThat(response.items().get(0).productName()).isEqualTo("MacBook Air M3");
        assertThat(response.subtotal()).isEqualByComparingTo("19998.00");
    }

    @Test
    void addItemIncreasesQuantityWhenProductAlreadyExists() {
        User user = user();
        Product product = product();
        Cart cart = cart(user);
        CartItem existing = cartItem(cart, product, 2);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryService.getAvailableQuantity(product.getId())).thenReturn(10);
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())).thenReturn(Optional.of(existing));
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId())).thenReturn(List.of(cartItem(cart, product, 5)));
        when(cartItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.addItem(new AddCartItemRequest(product.getId(), 3));

        assertThat(response.totalItemCount()).isEqualTo(5);
        assertThat(response.subtotal()).isEqualByComparingTo("49995.00");
    }

    @Test
    void addInactiveProductRejected() {
        User user = user();
        Product product = product();
        product.setActive(false);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart(user)));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(new AddCartItemRequest(product.getId(), 1)));
    }

    @Test
    void addProductBeyondAvailableStockRejected() {
        User user = user();
        Product product = product();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart(user)));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryService.getAvailableQuantity(product.getId())).thenReturn(2);

        assertThrows(ConflictException.class, () -> cartService.addItem(new AddCartItemRequest(product.getId(), 3)));
    }

    @Test
    void addProductWithoutInventoryRejected() {
        User user = user();
        Product product = product();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart(user)));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryService.getAvailableQuantity(product.getId()))
            .thenThrow(new ResourceNotFoundException("Inventory not found"));

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(new AddCartItemRequest(product.getId(), 1)));
    }

    @Test
    void addInactiveProductRejectedWithNotFound() {
        User user = user();
        Product product = product();
        product.setActive(false);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart(user)));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(ResourceNotFoundException.class, () -> cartService.addItem(new AddCartItemRequest(product.getId(), 1)));
    }

    @Test
    void updateMissingItemRejected() {
        User user = user();
        Product product = product();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart(user)));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryService.getAvailableQuantity(product.getId())).thenReturn(10);
        when(cartItemRepository.findByCartIdAndProductId(any(), any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.updateItemQuantity(product.getId(), new UpdateCartItemRequest(2)));
    }

    @Test
    void updateItemBeyondAvailableStockRejected() {
        User user = user();
        Product product = product();
        Cart cart = cart(user);
        CartItem existing = cartItem(cart, product, 1);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryService.getAvailableQuantity(product.getId())).thenReturn(2);
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () -> cartService.updateItemQuantity(product.getId(), new UpdateCartItemRequest(3)));
    }

    @Test
    void removeMissingItemStillTargetsUserCart() {
        User user = user();
        Cart cart = cart(user);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        cartService.removeItem(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        verify(cartItemRepository).deleteByCartIdAndProductId(cart.getId(), UUID.fromString("11111111-1111-1111-1111-111111111111"));
    }

    @Test
    void removeItemDeletesByProduct() {
        User user = user();
        Cart cart = cart(user);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        cartService.removeItem(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        verify(cartItemRepository).deleteByCartIdAndProductId(cart.getId(), UUID.fromString("11111111-1111-1111-1111-111111111111"));
    }

    private User user() {
        User user = new User();
        user.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        user.setEmail("customer@example.com");
        return user;
    }

    private Cart cart(User user) {
        Cart cart = new Cart();
        cart.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        cart.setUser(user);
        cart.setCreatedAt(Instant.now());
        cart.setUpdatedAt(Instant.now());
        cart.setItems(new ArrayList<>());
        return cart;
    }

    private Cart cart(Cart cart, List<CartItem> items) {
        cart.setItems(new ArrayList<>(items));
        return cart;
    }

    private Product product() {
        Product product = new Product();
        product.setId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
        product.setName("MacBook Air M3");
        product.setSku("MBA-M3-256");
        product.setPrice(new BigDecimal("9999.00"));
        product.setActive(true);
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

    private CartItem cartItem(Cart cart, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"));
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        return item;
    }
}
