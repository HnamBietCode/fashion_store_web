package com.fashionstore.fashion_store.controller.admin;

import com.fashionstore.fashion_store.entity.Product;
import com.fashionstore.fashion_store.service.CategoryService;
import com.fashionstore.fashion_store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String listProducts(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("products", productService.getAllProducts(PageRequest.of(page, 10)));
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        return "admin/products/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        return "admin/products/form";
    }

    @PostMapping("/save")
    public String saveProduct(@RequestParam String name,
            @RequestParam String slug,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) BigDecimal salePrice,
            @RequestParam String imageUrl,
            @RequestParam Integer stockQuantity,
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "false") boolean featured,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) Long id,
            RedirectAttributes redirectAttributes) {
        try {
            productService.saveProduct(id, name, slug, description, price, salePrice,
                    imageUrl, stockQuantity, categoryId, featured, active);
            redirectAttributes.addFlashAttribute("message", "Lưu sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        productService.getProductById(id).ifPresent(product -> {
            model.addAttribute("product", product);
        });
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        return "admin/products/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("message", "Xóa sản phẩm thành công!");
        return "redirect:/admin/products";
    }
}