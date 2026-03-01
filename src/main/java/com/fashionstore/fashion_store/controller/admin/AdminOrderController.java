package com.fashionstore.fashion_store.controller.admin;

import com.fashionstore.fashion_store.entity.Order;
import com.fashionstore.fashion_store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public String listOrders(@RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            Model model) {

        List<Order> allOrders = orderService.getAllOrders();

        // Filter by status
        List<Order> orders = allOrders.stream()
                .filter(o -> status == null || status.isBlank() || o.getStatus().name().equals(status))
                .filter(o -> q == null || q.isBlank()
                        || o.getShippingName().toLowerCase().contains(q.toLowerCase())
                        || o.getShippingPhone().contains(q)
                        || o.getOrderNumber().toLowerCase().contains(q.toLowerCase()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("q", q);

        // Badge counts per status
        model.addAttribute("cntPending", countByStatus(allOrders, "PENDING"));
        model.addAttribute("cntConfirmed", countByStatus(allOrders, "CONFIRMED"));
        model.addAttribute("cntShipping", countByStatus(allOrders, "SHIPPING"));
        model.addAttribute("cntDelivered", countByStatus(allOrders, "DELIVERED"));
        model.addAttribute("cntCancelled", countByStatus(allOrders, "CANCELLED"));

        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        orderService.getOrderById(id).ifPresent(order -> model.addAttribute("order", order));
        return "admin/orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, Order.OrderStatus.valueOf(status));
            redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    private long countByStatus(List<Order> orders, String status) {
        return orders.stream().filter(o -> o.getStatus().name().equals(status)).count();
    }
}