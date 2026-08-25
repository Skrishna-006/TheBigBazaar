package com.siva.shopsphere.orders.mapper;

import java.util.List;

import com.siva.shopsphere.orders.dto.OrderItemResponse;
import com.siva.shopsphere.orders.dto.OrderResponse;
import com.siva.shopsphere.orders.entity.Order;
import com.siva.shopsphere.orders.entity.OrderItem;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
            .map(OrderMapper::toResponse)
            .toList();
        return new OrderResponse(
            order.getId(),
            order.getStatus(),
            items,
            order.getSubtotal(),
            order.getShippingAmount(),
            order.getDiscountAmount(),
            order.getTotalAmount(),
            new OrderResponse.ShippingAddressSnapshot(
                order.getShippingAddress().getId(),
                order.getShippingFullName(),
                order.getShippingPhoneNumber(),
                order.getShippingAddressLine1(),
                order.getShippingAddressLine2(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingPostalCode(),
                order.getShippingCountry()
            ),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }

    public static OrderItemResponse toResponse(OrderItem item) {
        return new OrderItemResponse(
            item.getProduct().getId(),
            item.getProductName(),
            item.getProductSku(),
            item.getProductImageUrl(),
            item.getUnitPrice(),
            item.getQuantity(),
            item.getLineTotal()
        );
    }
}
