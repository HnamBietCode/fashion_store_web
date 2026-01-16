package com.fashionstore.fashion_store.controller;

import com.fashionstore.fashion_store.entity.Order;
import com.fashionstore.fashion_store.entity.User;
import com.fashionstore.fashion_store.repository.UserRepository;
import com.fashionstore.fashion_store.service.CartService;
import com.fashionstore.fashion_store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getUser(userDetails);

        if (cartService.getCartItems(user.getId()).isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", cartService.getCartItems(user.getId()));
        model.addAttribute("cartTotal", cartService.getCartTotal(user.getId()));
        model.addAttribute("user", user);
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String shippingName,
            @RequestParam String shippingPhone,
            @RequestParam String shippingAddress,
            @RequestParam(required = false) String note,
            @RequestParam String paymentMethod,
            RedirectAttributes redirectAttributes) {
        User user = getUser(userDetails);

        try {
            Order order = orderService.createOrder(
                    user.getId(),
                    shippingName,
                    shippingPhone,
                    shippingAddress,
                    note,
                    Order.PaymentMethod.valueOf(paymentMethod));
            return "redirect:/orders/" + order.getOrderNumber() + "/success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/orders/{orderNumber}/success")
    public String orderSuccess(@PathVariable String orderNumber, Model model) {
        orderService.getOrderByNumber(orderNumber).ifPresent(order -> {
            model.addAttribute("order", order);
        });
        return "order/success";
    }

    @GetMapping("/orders")
    public String orderHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getUser(userDetails);
        model.addAttribute("orders", orderService.getOrdersByUser(user.getId()));
        return "order/history";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        orderService.getOrderById(id).ifPresent(order -> {
            model.addAttribute("order", order);
        });
        return "order/detail";
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}