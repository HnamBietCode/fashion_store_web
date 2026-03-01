package com.fashionstore.fashion_store.controller.admin;

import com.fashionstore.fashion_store.repository.*;
import com.fashionstore.fashion_store.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        List<Order> deliveredOrders = orderRepository.findByStatus(Order.OrderStatus.DELIVERED);
        BigDecimal totalRevenue = deliveredOrders.stream()
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

        // ===== Dữ liệu biểu đồ doanh thu 12 tháng gần nhất =====
        // Group đơn đã giao theo tháng
        LocalDateTime now = LocalDateTime.now();
        List<String> chartLabels = new ArrayList<>();
        List<BigDecimal> chartData = new ArrayList<>();

        List<Order> allDelivered = orderRepository.findByStatus(Order.OrderStatus.DELIVERED);

        for (int i = 11; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);

            // Label: "Th1/2025", "Th2/2025" ...
            chartLabels.add("Th" + monthStart.getMonthValue() + "/" + monthStart.getYear());

            BigDecimal monthRevenue = allDelivered.stream()
                    .filter(o -> {
                        LocalDateTime t = o.getCreatedAt();
                        return t != null && !t.isBefore(monthStart) && t.isBefore(monthEnd);
                    })
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            chartData.add(monthRevenue);
        }

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);

        return "admin/dashboard";
    }
}