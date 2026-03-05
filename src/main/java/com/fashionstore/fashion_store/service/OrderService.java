package com.fashionstore.fashion_store.service;

import com.fashionstore.fashion_store.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {

        Order createOrder(Long userId, String shippingName, String shippingPhone,
                        String shippingAddress, String note, Order.PaymentMethod paymentMethod);

        Order createOrder(Long userId, String shippingName, String shippingPhone,
                        String shippingAddress, String note, Order.PaymentMethod paymentMethod,
                        java.math.BigDecimal discountAmount);

        List<Order> getOrdersByUser(Long userId);

        Optional<Order> getOrderById(Long orderId);

        Optional<Order> getOrderByNumber(String orderNumber);

        Order updateStatus(Long orderId, Order.OrderStatus status);

        List<Order> getAllOrders();
}