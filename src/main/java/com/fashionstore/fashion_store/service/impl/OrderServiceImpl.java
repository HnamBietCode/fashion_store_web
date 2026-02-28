package com.fashionstore.fashion_store.service.impl;

import com.fashionstore.fashion_store.entity.*;
import com.fashionstore.fashion_store.repository.OrderRepository;
import com.fashionstore.fashion_store.repository.UserRepository;
import com.fashionstore.fashion_store.service.CartService;
import com.fashionstore.fashion_store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import com.fashionstore.fashion_store.exception.BusinessException;
import com.fashionstore.fashion_store.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    @Override
    public Order createOrder(Long userId, String shippingName, String shippingPhone,
            String shippingAddress, String note, Order.PaymentMethod paymentMethod) {
        // COLLECTIONS: dùng List<CartItem> — interface, không dùng ArrayList cụ thể
        // → Polymorphism: biến kiểu List có thể là ArrayList, LinkedList...
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<CartItem> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            // BusinessException thay vì RuntimeException — rõ ràng hơn, có errorCode
            throw new BusinessException("CART_EMPTY", "Giỏ hàng trống! Vui lòng thêm sản phẩm.");
        }

        // Tính tổng tiền
        BigDecimal totalAmount = cartService.getCartTotal(userId);

        // Tạo order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.PENDING)
                .shippingName(shippingName)
                .shippingPhone(shippingPhone)
                .shippingAddress(shippingAddress)
                .note(note)
                .paymentMethod(paymentMethod)
                .items(new ArrayList<>())
                .build();

        // Tạo order items từ cart
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            BigDecimal price = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .price(price)
                    .quantity(cartItem.getQuantity())
                    .size(cartItem.getVariant() != null ? cartItem.getVariant().getSize() : null)
                    .color(cartItem.getVariant() != null ? cartItem.getVariant().getColor() : null)
                    .build();

            order.getItems().add(orderItem);
        }

        // Lưu order
        Order savedOrder = orderRepository.save(order);

        // Xóa giỏ hàng
        cartService.clearCart(userId);

        return savedOrder;
    }

    @Override
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public Order updateStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "FS" + timestamp + random;
    }
}