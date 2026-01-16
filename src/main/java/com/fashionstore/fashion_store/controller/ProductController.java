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
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (search != null && !search.isEmpty()) {
            model.addAttribute("products", productService.searchProducts(search, pageable));
            model.addAttribute("search", search);
        } else if (category != null) {
            model.addAttribute("products", productService.getProductsByCategory(category, pageable));
            model.addAttribute("selectedCategory", category);
        } else {
            model.addAttribute("products", productService.getAllProducts(pageable));
        }

        model.addAttribute("categories", categoryService.getAllActiveCategories());
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