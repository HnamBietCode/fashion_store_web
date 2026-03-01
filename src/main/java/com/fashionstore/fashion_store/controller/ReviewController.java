package com.fashionstore.fashion_store.controller;

import com.fashionstore.fashion_store.entity.User;
import com.fashionstore.fashion_store.repository.UserRepository;
import com.fashionstore.fashion_store.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @PostMapping("/{productId}/review")
    public String submitReview(@PathVariable Long productId,
            @RequestParam int rating,
            @RequestParam(required = false) String comment,
            @RequestParam String productSlug,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            reviewService.addReview(user.getId(), productId, rating, comment);
            redirectAttributes.addFlashAttribute("reviewSuccess", "Cảm ơn bạn đã đánh giá sản phẩm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reviewError", "Lỗi: " + e.getMessage());
        }
        return "redirect:/products/" + productSlug;
    }
}
