package com.fashionstore.fashion_store.controller;

import com.fashionstore.fashion_store.entity.User;
import com.fashionstore.fashion_store.repository.UserRepository;
import com.fashionstore.fashion_store.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    @GetMapping
    public String viewWishlist(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getUser(userDetails);
        model.addAttribute("wishlistItems", wishlistService.getUserWishlist(user.getId()));
        return "wishlist/list";
    }

    @PostMapping("/toggle/{productId}")
    public String toggle(@PathVariable Long productId,
            @RequestParam(required = false) String redirect,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes ra) {
        User user = getUser(userDetails);
        boolean added = wishlistService.toggleWishlist(user.getId(), productId);
        ra.addFlashAttribute("wishlistMsg",
                added ? "❤️ Đã thêm vào danh sách yêu thích!" : "💔 Đã xóa khỏi danh sách yêu thích!");
        // Redirect về trang gọi toggle (product detail hoặc wishlist)
        return redirect != null && !redirect.isBlank()
                ? "redirect:" + redirect
                : "redirect:/wishlist";
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
