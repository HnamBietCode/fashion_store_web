package com.fashionstore.fashion_store.controller.admin;

import com.fashionstore.fashion_store.repository.*;
import com.fashionstore.fashion_store.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("totalCategories", categoryRepository.count());
        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("totalUsers", userRepository.count());

        // Tổng doanh thu từ các đơn đã giao
        BigDecimal totalRevenue = orderRepository.findByStatus(Order.OrderStatus.DELIVERED)
                .stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalRevenue", totalRevenue);

        // Số đơn chờ xác nhận
        long pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING).size();
        model.addAttribute("pendingOrders", pendingOrders);

        // 10 đơn gần nhất
        model.addAttribute("recentOrders", orderRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10).toList());
        return "admin/dashboard";
    }
}