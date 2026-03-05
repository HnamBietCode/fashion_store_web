package com.fashionstore.fashion_store.controller;

import com.fashionstore.fashion_store.entity.Order;
import com.fashionstore.fashion_store.entity.User;
import com.fashionstore.fashion_store.repository.UserRepository;
import com.fashionstore.fashion_store.service.CartService;
import com.fashionstore.fashion_store.service.CouponService;
import com.fashionstore.fashion_store.service.EmailService;
import com.fashionstore.fashion_store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final CouponService couponService;

    // ==================== CHECKOUT ====================

    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getUser(userDetails);

        var cartItems = cartService.getCartItems(user.getId());
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartService.getCartTotal(user.getId()));
        model.addAttribute("user", user);
        return "checkout/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            @RequestParam String shippingName,
            @RequestParam String shippingPhone,
            @RequestParam String shippingAddress,
            @RequestParam(required = false) String note,
            @RequestParam(defaultValue = "COD") String paymentMethod,
            @RequestParam(required = false) String couponCode,
            RedirectAttributes redirectAttributes) {
        User user = getUser(userDetails);
        try {
            // Validate coupon nếu có
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (couponCode != null && !couponCode.isBlank()) {
                BigDecimal cartTotal = cartService.getCartTotal(user.getId());
                try {
                    discountAmount = couponService.validateAndCalculate(couponCode, cartTotal);
                    couponService.incrementUsage(couponCode);
                } catch (Exception ce) {
                    redirectAttributes.addFlashAttribute("error", "Mã giảm giá: " + ce.getMessage());
                    return "redirect:/checkout";
                }
            }

            Order.PaymentMethod method = Order.PaymentMethod.valueOf(paymentMethod);

            Order order = orderService.createOrder(
                    user.getId(),
                    shippingName,
                    shippingPhone,
                    shippingAddress,
                    note,
                    method,
                    discountAmount);

            // Thanh toán COD / BANK_TRANSFER: xử lý như cũ
            emailService.sendOrderConfirmation(order);

            if (Order.PaymentMethod.PAYPAL.equals(method)) {
                return "redirect:/paypal-checkout?orderNumber=" + order.getOrderNumber();
            }

            redirectAttributes.addFlashAttribute("orderNumber", order.getOrderNumber());
            redirectAttributes.addFlashAttribute("orderTotal", order.getTotalAmount());
            if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                redirectAttributes.addFlashAttribute("discountAmount", discountAmount);
            }
            return "redirect:/order-success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đặt hàng thất bại: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    // ==================== ORDER SUCCESS ====================

    @GetMapping("/order-success")
    public String orderSuccess() {
        return "orders/success";
    }

    // ==================== ORDER HISTORY ====================

    @GetMapping("/orders")
    public String orderHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getUser(userDetails);
        model.addAttribute("orders", orderService.getOrdersByUser(user.getId()));
        return "orders/list";
    }

    @GetMapping("/orders/{orderNumber}")
    public String orderDetail(@PathVariable String orderNumber,
            @AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getUser(userDetails);
        orderService.getOrderByNumber(orderNumber).ifPresent(order -> {
            if (order.getUser().getId().equals(user.getId())) {
                model.addAttribute("order", order);
            }
        });
        return "orders/detail";
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}