package com.fashionstore.fashion_store.controller;

import com.fashionstore.fashion_store.entity.User;
import com.fashionstore.fashion_store.repository.UserRepository;
import com.fashionstore.fashion_store.service.CategoryService;
import com.fashionstore.fashion_store.service.ProductService;
import com.fashionstore.fashion_store.service.ReviewService;
import com.fashionstore.fashion_store.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String search, // từ form sidebar
            @RequestParam(required = false) String keyword, // từ navbar autocomplete
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "newest") String sort,
            Model model) {

        // keyword (navbar) ưu tiên hơn search (sidebar form)
        String q = (keyword != null && !keyword.isBlank()) ? keyword
                : (search != null && !search.isBlank()) ? search
                        : null;

        Sort sorting = switch (sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "name" -> Sort.by("name").ascending();
            default -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sorting);

        var products = productService.filterProducts(category, q, minPrice, maxPrice, pageable);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("search", q); // hiển thị lại trong ô sidebar
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        return "products/list";
    }

    @GetMapping("/products/{slug}")
    public String productDetail(@PathVariable String slug,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        productService.getProductBySlug(slug).ifPresent(product -> {
            model.addAttribute("product", product);
            model.addAttribute("relatedProducts",
                    productService.getProductsByCategory(product.getCategory().getId(),
                            PageRequest.of(0, 4)).getContent());

            // Reviews
            model.addAttribute("reviews", reviewService.getProductReviews(product.getId()));
            model.addAttribute("avgRating", reviewService.getAverageRating(product.getId()));
            model.addAttribute("reviewCount", reviewService.getReviewCount(product.getId()));

            // User-specific data (nếu đã đăng nhập)
            if (userDetails != null) {
                userRepository.findByEmail(userDetails.getUsername()).ifPresent(user -> {
                    model.addAttribute("hasReviewed",
                            reviewService.hasUserReviewed(user.getId(), product.getId()));
                    model.addAttribute("inWishlist",
                            wishlistService.isInWishlist(user.getId(), product.getId()));
                });
            }
        });
        return "products/detail";
    }
}