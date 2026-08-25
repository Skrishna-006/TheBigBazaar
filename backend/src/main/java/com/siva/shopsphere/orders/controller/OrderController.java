package com.siva.shopsphere.orders.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.siva.shopsphere.orders.dto.CreateOrderRequest;
import com.siva.shopsphere.orders.dto.OrderResponse;
import com.siva.shopsphere.orders.dto.UpdateOrderStatusRequest;
import com.siva.shopsphere.orders.entity.OrderStatus;
import com.siva.shopsphere.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearer-jwt")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create an order from the current user's cart")
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @Operation(summary = "Get current user's orders")
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    @Operation(summary = "Get a single order belonging to the current user")
    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getMyOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getMyOrder(id));
    }

    @Operation(summary = "Cancel the current user's order")
    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    @Operation(summary = "Get all orders for admins")
    @GetMapping("/admin/orders")
    public ResponseEntity<List<OrderResponse>> getAdminOrders(@RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(orderService.getAdminOrders(status));
    }

    @Operation(summary = "Get any order for admins")
    @GetMapping("/admin/orders/{id}")
    public ResponseEntity<OrderResponse> getAdminOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getAdminOrder(id));
    }

    @Operation(summary = "Update order status for admins")
    @PatchMapping("/admin/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable UUID id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }
}
