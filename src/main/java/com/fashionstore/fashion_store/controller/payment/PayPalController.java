package com.fashionstore.fashion_store.controller.payment;

import com.fashionstore.fashion_store.entity.Order;
import com.fashionstore.fashion_store.service.OrderService;
import com.fashionstore.fashion_store.service.PayPalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PayPalController {

    private final OrderService orderService;
    private final PayPalService payPalService;

    private static final BigDecimal EXCHANGE_RATE = new BigDecimal("24000");

    @GetMapping("/paypal-checkout")
    public String showPaypalCheckout(@RequestParam String orderNumber,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null)
            return "redirect:/login";

        Optional<Order> orderOpt = orderService.getOrderByNumber(orderNumber);
        if (orderOpt.isEmpty() || orderOpt.get().getStatus() != Order.OrderStatus.PENDING) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không hợp lệ!");
            return "redirect:/orders";
        }

        Order order = orderOpt.get();
        BigDecimal totalUsd = order.getTotalAmount().divide(EXCHANGE_RATE, 2, RoundingMode.HALF_UP);

        try {
            String approveUrl = payPalService.createOrderRequest(totalUsd.toString(), order.getOrderNumber());
            return "redirect:" + approveUrl;
        } catch (Exception e) {
            log.error("Error redirecting to PayPal checkout", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi khởi tạo thanh toán PayPal.");
            return "redirect:/cart";
        }
    }

    @GetMapping("/paypal/success")
    public String paypalSuccess(@RequestParam("token") String token,
            @RequestParam("orderNumber") String orderNumber,
            RedirectAttributes redirectAttributes) {
        // token is the PayPal Order ID passed back from PayPal
        boolean captured = payPalService.captureOrder(token);

        if (captured) {
            orderService.getOrderByNumber(orderNumber).ifPresent(order -> {
                if (order.getStatus() == Order.OrderStatus.PENDING) {
                    orderService.updateStatus(order.getId(), Order.OrderStatus.CONFIRMED);
                }
            });
            redirectAttributes.addFlashAttribute("orderNumber", orderNumber);
            redirectAttributes.addFlashAttribute("vnpaySuccess", true); // Reusing existing success view logic
            return "redirect:/order-success";
        } else {
            redirectAttributes.addFlashAttribute("error", "Thanh toán PayPal không thành công (Hủy hoặc lỗi duyệt).");
            return "redirect:/cart";
        }
    }

    @GetMapping("/paypal/cancel")
    public String paypalCancel(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Bạn đã hủy quá trình thanh toán PayPal.");
        return "redirect:/cart";
    }
}
