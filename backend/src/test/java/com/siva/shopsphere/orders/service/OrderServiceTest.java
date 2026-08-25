package com.siva.shopsphere.orders.service;

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

import com.siva.shopsphere.accounts.entity.Address;
import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.repository.AddressRepository;
import com.siva.shopsphere.cart.entity.Cart;
import com.siva.shopsphere.cart.entity.CartItem;
import com.siva.shopsphere.cart.repository.CartItemRepository;
import com.siva.shopsphere.cart.repository.CartRepository;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.inventory.entity.Inventory;
import com.siva.shopsphere.inventory.repository.InventoryMovementRepository;
import com.siva.shopsphere.inventory.repository.InventoryRepository;
import com.siva.shopsphere.orders.dto.CreateOrderRequest;
import com.siva.shopsphere.orders.dto.OrderResponse;
import com.siva.shopsphere.orders.entity.Order;
import com.siva.shopsphere.orders.entity.OrderItem;
import com.siva.shopsphere.orders.entity.OrderStatus;
import com.siva.shopsphere.orders.repository.OrderRepository;
import com.siva.shopsphere.products.entity.Brand;
import com.siva.shopsphere.products.entity.Category;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void successfulOrderCreationSnapshotsDataAndClearsCart() {
        User user = user();
        Address address = address(user);
        Product product = product(true, new BigDecimal("1000.00"));
        Cart cart = cart(user);
        CartItem item = cartItem(cart, product, 2);
        Inventory inventory = inventory(product, 5);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByIdAndUserId(address.getId(), user.getId())).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId())).thenReturn(List.of(item));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdWithLock(product.getId())).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return order;
        });

        OrderResponse response = orderService.createOrder(new CreateOrderRequest(address.getId()));

        assertThat(response.totalAmount()).isEqualByComparingTo("2000.00");
        assertThat(response.subtotal()).isEqualByComparingTo("2000.00");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).productName()).isEqualTo("MacBook Air M3");
        verify(cartItemRepository).deleteAllByCartId(cart.getId());
        assertThat(inventory.getQuantity()).isEqualTo(3);
    }

    @Test
    void emptyCartRejected() {
        User user = user();
        Address address = address(user);
        Cart cart = cart(user);
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByIdAndUserId(address.getId(), user.getId())).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId())).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> orderService.createOrder(new CreateOrderRequest(address.getId())));
    }

    @Test
    void missingAddressRejected() {
        User user = user();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByIdAndUserId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"), user.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(new CreateOrderRequest(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"))));
    }

    @Test
    void inactiveProductRejectedAndCartNotCleared() {
        User user = user();
        Address address = address(user);
        Product product = product(false, new BigDecimal("1000.00"));
        Cart cart = cart(user);
        CartItem item = cartItem(cart, product, 1);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByIdAndUserId(address.getId(), user.getId())).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId())).thenReturn(List.of(item));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThrows(ConflictException.class, () -> orderService.createOrder(new CreateOrderRequest(address.getId())));
    }

    @Test
    void insufficientInventoryRejected() {
        User user = user();
        Address address = address(user);
        Product product = product(true, new BigDecimal("1000.00"));
        Cart cart = cart(user);
        CartItem item = cartItem(cart, product, 5);
        Inventory inventory = inventory(product, 2);

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(addressRepository.findByIdAndUserId(address.getId(), user.getId())).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId())).thenReturn(List.of(item));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductIdWithLock(product.getId())).thenReturn(Optional.of(inventory));

        assertThrows(ConflictException.class, () -> orderService.createOrder(new CreateOrderRequest(address.getId())));
    }

    @Test
    void customerOnlySeesOwnOrders() {
        User user = user();
        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of(order(user)));

        assertThat(orderService.getMyOrders()).hasSize(1);
    }

    @Test
    void cancelOrderRestoresInventory() {
        User user = user();
        Order order = order(user);
        order.setStatus(OrderStatus.CONFIRMED);
        Product product = product(true, new BigDecimal("1000.00"));
        Inventory inventory = inventory(product, 1);
        OrderItem item = orderItem(order, product, 1, new BigDecimal("1000.00"));
        order.setItems(List.of(item));

        when(currentUserService.getCurrentUser()).thenReturn(user);
        when(orderRepository.findByIdAndUserId(order.getId(), user.getId())).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductIdWithLock(product.getId())).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.cancelOrder(order.getId());

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(inventory.getQuantity()).isEqualTo(2);
    }

    private User user() {
        User user = new User();
        user.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        user.setEmail("customer@example.com");
        return user;
    }

    private Address address(User user) {
        Address address = new Address();
        address.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        address.setUser(user);
        address.setFirstName("Siva");
        address.setLastName("Krishna");
        address.setPhone("9876543210");
        address.setAddressLine1("Main Road");
        address.setCity("Anantapur");
        address.setState("AP");
        address.setPostalCode("515001");
        address.setCountry("India");
        return address;
    }

    private Cart cart(User user) {
        Cart cart = new Cart();
        cart.setId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
        cart.setUser(user);
        cart.setItems(List.of());
        cart.setCreatedAt(Instant.now());
        cart.setUpdatedAt(Instant.now());
        return cart;
    }

    private Product product(boolean active, BigDecimal price) {
        Product product = new Product();
        product.setId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));
        product.setName("MacBook Air M3");
        product.setSku("MBA-M3-256");
        product.setSlug("macbook-air-m3");
        product.setPrice(price);
        product.setActive(active);
        Category category = new Category();
        category.setId(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"));
        category.setName("Electronics");
        category.setSlug("electronics");
        Brand brand = new Brand();
        brand.setId(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"));
        brand.setName("Apple");
        brand.setSlug("apple");
        product.setCategory(category);
        product.setBrand(brand);
        return product;
    }

    private Inventory inventory(Product product, int quantity) {
        Inventory inventory = new Inventory();
        inventory.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        inventory.setProduct(product);
        inventory.setQuantity(quantity);
        inventory.setReservedQuantity(0);
        inventory.setLowStockThreshold(1);
        return inventory;
    }

    private Order order(User user) {
        Order order = new Order();
        order.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        order.setUser(user);
        order.setShippingAddress(address(user));
        order.setShippingFullName("Siva Krishna");
        order.setShippingPhoneNumber("9876543210");
        order.setShippingAddressLine1("Main Road");
        order.setShippingCity("Anantapur");
        order.setShippingState("AP");
        order.setShippingPostalCode("515001");
        order.setShippingCountry("India");
        order.setStatus(OrderStatus.CONFIRMED);
        order.setSubtotal(new BigDecimal("1000.00"));
        order.setShippingAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(new BigDecimal("1000.00"));
        return order;
    }

    private OrderItem orderItem(Order order, Product product, int quantity, BigDecimal unitPrice) {
        OrderItem item = new OrderItem();
        item.setId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        item.setOrder(order);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setProductSku(product.getSku());
        item.setProductImageUrl(product.getImageUrl());
        item.setUnitPrice(unitPrice);
        item.setQuantity(quantity);
        item.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    private CartItem cartItem(Cart cart, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setId(UUID.fromString("44444444-4444-4444-4444-444444444444"));
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        return item;
    }
}
