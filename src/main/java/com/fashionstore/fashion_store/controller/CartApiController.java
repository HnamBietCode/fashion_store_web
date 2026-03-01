package com.fashionstore.fashion_store.controller;

import com.fashionstore.fashion_store.entity.User;
import com.fashionstore.fashion_store.repository.UserRepository;
import com.fashionstore.fashion_store.service.CartService;
import com.fashionstore.fashion_store.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST API cho AJAX cart operations — trả JSON không reload trang
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartApiController {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final CouponService couponService;

    /** AJAX Add to Cart — trả về {success, message, cartCount} */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getUser(userDetails);
            cartService.addToCart(user.getId(), productId, variantId, quantity);
            int count = cartService.getCartItemCount(user.getId());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã thêm vào giỏ hàng!",
                    "cartCount", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /** AJAX Validate coupon — trả về {valid, discount, message} */
    @PostMapping("/validate-coupon")
    public ResponseEntity<Map<String, Object>> validateCoupon(
            @RequestParam String code,
            @RequestParam BigDecimal orderAmount,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            BigDecimal discount = couponService.validateAndCalculate(code, orderAmount);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "discount", discount,
                    "message", "Áp dụng mã thành công! Giảm " + discount.longValue() + "đ"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "discount", 0,
                    "message", e.getMessage()));
        }
    }

    private User getUser(UserDetails userDetails) {
        if (userDetails == null)
            throw new RuntimeException("Vui lòng đăng nhập!");
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
