package com.fashionstore.fashion_store.controller;

import com.fashionstore.fashion_store.service.CategoryService;
import com.fashionstore.fashion_store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "newest") String sort,
            Model model) {

        // Sắp xếp
        Sort sorting = switch (sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "name" -> Sort.by("name").ascending();
            default -> Sort.by("createdAt").descending(); // newest
        };

        Pageable pageable = PageRequest.of(page, size, sorting);

        // Dùng filterProducts() duy nhất cho tất cả trường hợp
        var products = productService.filterProducts(category, search, minPrice, maxPrice, pageable);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllActiveCategories());

        // Giữ lại các filter params để hiển thị trên UI
        model.addAttribute("selectedCategory", category);
        model.addAttribute("search", search);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        return "products/list";
    }

    @GetMapping("/products/{slug}")
    public String productDetail(@PathVariable String slug, Model model) {
        productService.getProductBySlug(slug).ifPresent(product -> {
            model.addAttribute("product", product);
            model.addAttribute("relatedProducts",
                    productService.getProductsByCategory(product.getCategory().getId(),
                            PageRequest.of(0, 4)).getContent());
        });
        return "products/detail";
    }
}