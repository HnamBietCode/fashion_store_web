package com.fashionstore.fashion_store.service.impl;

import com.fashionstore.fashion_store.entity.*;
import com.fashionstore.fashion_store.repository.OrderRepository;
import com.fashionstore.fashion_store.repository.ProductRepository;
import com.fashionstore.fashion_store.repository.ProductVariantRepository;
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
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

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

        // Tính phí Vận chuyển
        BigDecimal shippingFee = new BigDecimal("30000"); // Mặc định 30k
        String lowerAddress = shippingAddress != null ? shippingAddress.toLowerCase() : "";
        if (lowerAddress.contains("hà nội") || lowerAddress.contains("hồ chí minh")) {
            shippingFee = new BigDecimal("20000");
        }

        // Cộng phí ship vào tổng thanh toán đơn hàng
        totalAmount = totalAmount.add(shippingFee);

        // Tạo order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .totalAmount(totalAmount)
                .shippingFee(shippingFee)
                .shippingName(shippingName)
                .shippingPhone(shippingPhone)
                .shippingAddress(shippingAddress)
                .note(note)
                .paymentMethod(paymentMethod)
                .items(new ArrayList<>())
                .build();

        // Check stock and set status
        processStockAndStatus(order, cartItems, paymentMethod == Order.PaymentMethod.PAYPAL);

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
    public Order createOrder(Long userId, String shippingName, String shippingPhone,
            String shippingAddress, String note, Order.PaymentMethod paymentMethod,
            BigDecimal discountAmount) {
        // Delegate to main method, then apply discount to saved order total
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<CartItem> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException("CART_EMPTY", "Giỏ hàng trống! Vui lòng thêm sản phẩm.");
        }

        BigDecimal totalAmount = cartService.getCartTotal(userId);

        // Tính phí Vận chuyển
        BigDecimal shippingFee = new BigDecimal("30000"); // Mặc định 30k
        String lowerAddress = shippingAddress != null ? shippingAddress.toLowerCase() : "";
        if (lowerAddress.contains("hà nội") || lowerAddress.contains("hồ chí minh")) {
            shippingFee = new BigDecimal("20000");
        }

        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;

        // Final Total = (Tổng giỏ hàng + Phí Ship) - Số tiền giảm giá
        BigDecimal finalTotal = totalAmount.add(shippingFee).subtract(discount).max(BigDecimal.ZERO);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .shippingFee(shippingFee)
                .totalAmount(finalTotal)
                .shippingName(shippingName)
                .shippingPhone(shippingPhone)
                .shippingAddress(shippingAddress)
                .note(note)
                .paymentMethod(paymentMethod)
                .items(new ArrayList<>())
                .build();

        // Check stock and set status
        processStockAndStatus(order, cartItems, paymentMethod == Order.PaymentMethod.PAYPAL);

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

        Order savedOrder = orderRepository.save(order);
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
    public Order updatePaymentMethod(Long orderId, Order.PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        order.setPaymentMethod(paymentMethod);
        return orderRepository.save(order);
    }

    @Override
    public Order confirmPayPalOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            return order;
        }

        boolean allInStock = true;
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (item.getSize() != null || item.getColor() != null) {
                ProductVariant matchedVariant = product.getVariants().stream()
                        .filter(v -> (v.getSize() != null ? v.getSize().equals(item.getSize()) : item.getSize() == null)
                                &&
                                (v.getColor() != null ? v.getColor().equals(item.getColor()) : item.getColor() == null))
                        .findFirst().orElse(null);
                if (matchedVariant != null && matchedVariant.getQuantity() < item.getQuantity()) {
                    allInStock = false;
                    break;
                }
            } else {
                if (product.getStockQuantity() < item.getQuantity()) {
                    allInStock = false;
                    break;
                }
            }
        }

        if (allInStock) {
            order.setStatus(Order.OrderStatus.SHIPPING);
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (item.getSize() != null || item.getColor() != null) {
                    ProductVariant matchedVariant = product.getVariants().stream()
                            .filter(v -> (v.getSize() != null ? v.getSize().equals(item.getSize())
                                    : item.getSize() == null) &&
                                    (v.getColor() != null ? v.getColor().equals(item.getColor())
                                            : item.getColor() == null))
                            .findFirst().orElse(null);
                    if (matchedVariant != null) {
                        matchedVariant.setQuantity(matchedVariant.getQuantity() - item.getQuantity());
                        productVariantRepository.save(matchedVariant);
                    }
                } else {
                    product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                    productRepository.save(product);
                }
            }
        } else {
            order.setStatus(Order.OrderStatus.CONFIRMED);
        }

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

    private void processStockAndStatus(Order order, List<CartItem> cartItems, boolean isPayPal) {
        order.setStatus(Order.OrderStatus.PENDING); // Default

        boolean allInStock = true;
        for (CartItem item : cartItems) {
            if (item.getVariant() != null) {
                if (item.getVariant().getQuantity() < item.getQuantity()) {
                    allInStock = false;
                    break;
                }
            } else {
                if (item.getProduct().getStockQuantity() < item.getQuantity()) {
                    allInStock = false;
                    break;
                }
            }
        }

        if (allInStock && !isPayPal) {
            order.setStatus(Order.OrderStatus.SHIPPING);
            // Deduct stock
            for (CartItem item : cartItems) {
                if (item.getVariant() != null) {
                    ProductVariant variant = item.getVariant();
                    variant.setQuantity(variant.getQuantity() - item.getQuantity());
                    productVariantRepository.save(variant);
                } else {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                    productRepository.save(product);
                }
            }
        }
    }
}