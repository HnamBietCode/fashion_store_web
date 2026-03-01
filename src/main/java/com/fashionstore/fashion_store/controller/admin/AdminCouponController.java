package com.fashionstore.fashion_store.controller.admin;

import com.fashionstore.fashion_store.entity.Coupon;
import com.fashionstore.fashion_store.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public String listCoupons(Model model) {
        model.addAttribute("coupons", couponService.getAllCoupons());
        return "admin/coupons/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("coupon", new Coupon());
        return "admin/coupons/form";
    }

    @PostMapping("/save")
    public String saveCoupon(
            @RequestParam String code,
            @RequestParam String description,
            @RequestParam Coupon.DiscountType discountType,
            @RequestParam BigDecimal discountValue,
            @RequestParam(required = false) BigDecimal minOrderAmount,
            @RequestParam(required = false) BigDecimal maxDiscount,
            @RequestParam(required = false) Integer usageLimit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) Long id,
            RedirectAttributes redirectAttributes) {
        try {
            Coupon coupon = Coupon.builder()
                    .id(id)
                    .code(code)
                    .description(description)
                    .discountType(discountType)
                    .discountValue(discountValue)
                    .minOrderAmount(minOrderAmount)
                    .maxDiscount(maxDiscount)
                    .usageLimit(usageLimit)
                    .usedCount(0)
                    .expiresAt(expiresAt)
                    .active(active)
                    .build();
            couponService.save(coupon);
            redirectAttributes.addFlashAttribute("message", "Lưu mã giảm giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/coupons";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        couponService.findByCode(id.toString())
                .ifPresentOrElse(
                        c -> model.addAttribute("coupon", c),
                        () -> {
                            // fallback: find by id via list
                        });
        // Use service list and find by id
        couponService.getAllCoupons().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .ifPresent(c -> model.addAttribute("coupon", c));
        return "admin/coupons/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteCoupon(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        couponService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Xóa mã giảm giá thành công!");
        return "redirect:/admin/coupons";
    }
}
