package com.siva.shopsphere.orders.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.siva.shopsphere.inventory.entity.InventoryMovement;
import com.siva.shopsphere.inventory.entity.InventoryMovementType;
import com.siva.shopsphere.inventory.repository.InventoryMovementRepository;
import com.siva.shopsphere.inventory.repository.InventoryRepository;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.security.CurrentUserService;
import com.siva.shopsphere.orders.dto.CreateOrderRequest;
import com.siva.shopsphere.orders.dto.OrderResponse;
import com.siva.shopsphere.orders.dto.UpdateOrderStatusRequest;
import com.siva.shopsphere.orders.entity.Order;
import com.siva.shopsphere.orders.entity.OrderItem;
import com.siva.shopsphere.orders.entity.OrderStatus;
import com.siva.shopsphere.orders.mapper.OrderMapper;
import com.siva.shopsphere.orders.repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final CurrentUserService currentUserService;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, AddressRepository addressRepository, ProductRepository productRepository, InventoryRepository inventoryRepository, InventoryMovementRepository movementRepository, CurrentUserService currentUserService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User user = currentUserService.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(request.addressId(), user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseThrow(() -> new BadRequestException("Cannot create an order from an empty cart."));
        List<CartItem> cartItems = cartItemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot create an order from an empty cart.");
        }

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setUser(user);
        order.setShippingAddress(address);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingFullName(address.getFirstName() + " " + address.getLastName());
        order.setShippingPhoneNumber(address.getPhone());
        order.setShippingAddressLine1(address.getAddressLine1());
        order.setShippingAddressLine2(address.getAddressLine2());
        order.setShippingCity(address.getCity());
        order.setShippingState(address.getState());
        order.setShippingPostalCode(address.getPostalCode());
        order.setShippingCountry(address.getCountry());
        order.setShippingAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                .filter(Product::isActive)
                .orElseThrow(() -> new ConflictException("The cart contains an unavailable product."));
            Inventory inventory = inventoryRepository.findByProductIdWithLock(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
            if (cartItem.getQuantity() > availableQuantity(inventory)) {
                throw new ConflictException("Requested quantity exceeds available stock");
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            inventory.setQuantity(inventory.getQuantity() - cartItem.getQuantity());
            inventoryRepository.save(inventory);
            createMovement(inventory, InventoryMovementType.STOCK_OUT, cartItem.getQuantity(), "ORDER:" + order.getId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductSku(product.getSku());
            orderItem.setProductImageUrl(product.getImageUrl());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setLineTotal(lineTotal);
            order.getItems().add(orderItem);
        }

        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal);
        Order saved = orderRepository.save(order);
        cartItemRepository.deleteAllByCartId(cart.getId());
        return OrderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User user = currentUserService.getCurrentUser();
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(OrderMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(UUID id) {
        User user = currentUserService.getCurrentUser();
        Order order = orderRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        User user = currentUserService.getCurrentUser();
        Order order = orderRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.PROCESSING) {
            throw new ConflictException("Order cannot be cancelled in its current state");
        }
        restoreInventory(order);
        order.setStatus(OrderStatus.CANCELLED);
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAdminOrders(OrderStatus status) {
        return (status == null ? orderRepository.findAllByOrderByCreatedAtDesc() : orderRepository.findAllByStatusOrderByCreatedAtDesc(status))
            .stream().map(OrderMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getAdminOrder(UUID id) {
        return orderRepository.findById(id).map(OrderMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        validateTransition(order.getStatus(), request.status());
        order.setStatus(request.status());
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    private int availableQuantity(Inventory inventory) {
        return inventory.getQuantity() - inventory.getReservedQuantity();
    }

    private InventoryMovement createMovement(Inventory inventory, InventoryMovementType type, int quantity, String reason) {
        InventoryMovement movement = new InventoryMovement();
        movement.setInventory(inventory);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setReason(reason);
        return movementRepository.save(movement);
    }

    private void restoreInventory(Order order) {
        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository.findByProductIdWithLock(item.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
            inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);
            createMovement(inventory, InventoryMovementType.STOCK_IN, item.getQuantity(), "CANCEL:" + order.getId());
        }
    }

    private void validateTransition(OrderStatus currentStatus, OrderStatus nextStatus) {
        if (currentStatus == nextStatus) {
            throw new ConflictException("Order is already in the requested status");
        }
        boolean allowed = switch (currentStatus) {
            case PENDING -> nextStatus == OrderStatus.CONFIRMED || nextStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> nextStatus == OrderStatus.PROCESSING || nextStatus == OrderStatus.CANCELLED;
            case PROCESSING -> nextStatus == OrderStatus.SHIPPED;
            case SHIPPED -> nextStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
        if (!allowed) {
            throw new ConflictException("Invalid order status transition");
        }
    }
}
