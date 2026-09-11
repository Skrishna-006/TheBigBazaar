package com.siva.shopsphere.cart.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.cart.dto.AddCartItemRequest;
import com.siva.shopsphere.cart.dto.CartResponse;
import com.siva.shopsphere.cart.dto.UpdateCartItemRequest;
import com.siva.shopsphere.cart.entity.Cart;
import com.siva.shopsphere.cart.entity.CartItem;
import com.siva.shopsphere.cart.mapper.CartMapper;
import com.siva.shopsphere.cart.repository.CartItemRepository;
import com.siva.shopsphere.cart.repository.CartRepository;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.inventory.service.InventoryService;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.security.CurrentUserService;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final CurrentUserService currentUserService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, InventoryService inventoryService, CurrentUserService currentUserService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public CartResponse getCart() {
        Cart cart = getOrCreateCart();
        return CartMapper.toResponse(reloadCart(cart.getId()));
    }

    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        if (request.quantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
        Cart cart = getOrCreateCart();
        Product product = loadActiveProduct(request.productId());
        int availableQuantity = inventoryService.getAvailableQuantity(product.getId());
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElseGet(CartItem::new);
        int newQuantity = item.getId() == null ? request.quantity() : item.getQuantity() + request.quantity();
        ensureStock(newQuantity, availableQuantity);
        if (item.getId() == null) {
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.quantity());
            cart.getItems().add(item);
        } else {
            item.setQuantity(newQuantity);
        }
        cartItemRepository.save(item);
        return CartMapper.toResponse(reloadCart(cart.getId()));
    }

    @Transactional
    public CartResponse updateItemQuantity(UUID productId, UpdateCartItemRequest request) {
        if (request.quantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
        Cart cart = getOrCreateCart();
        Product product = loadActiveProduct(productId);
        int availableQuantity = inventoryService.getAvailableQuantity(product.getId());
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        ensureStock(request.quantity(), availableQuantity);
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        return CartMapper.toResponse(reloadCart(cart.getId()));
    }

    @Transactional
    public void removeItem(UUID productId) {
        Cart cart = getOrCreateCart();
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
    }

    @Transactional
    public void clearCart() {
        Cart cart = getOrCreateCart();
        cartItemRepository.deleteAllByCartId(cart.getId());
    }

    private Cart getOrCreateCart() {
        User user = currentUserService.getCurrentUser();
        return cartRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                Cart cart = new Cart();
                cart.setUser(user);
                cart.setItems(new ArrayList<>());
                return cartRepository.save(cart);
            });
    }

    private Cart reloadCart(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        List<CartItem> items = cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(cartId);
        cart.getItems().clear();
        cart.getItems().addAll(items);
        return cart;
    }

    private Product loadActiveProduct(UUID productId) {
        return productRepository.findById(productId)
            .filter(Product::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private void ensureStock(int requestedQuantity, int availableQuantity) {
        if (requestedQuantity > availableQuantity) {
            throw new ConflictException("Requested quantity exceeds available stock");
        }
    }
}
