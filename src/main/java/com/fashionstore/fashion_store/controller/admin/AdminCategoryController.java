package com.fashionstore.fashion_store.controller.admin;

import com.fashionstore.fashion_store.entity.Category;
import com.fashionstore.fashion_store.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        categoryService.getCategoryById(id).ifPresent(c -> model.addAttribute("category", c));
        return "admin/categories/form";
    }

    @PostMapping("/save")
    public String saveCategory(@RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam String slug,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "true") boolean active,
            RedirectAttributes redirectAttributes) {
        try {
            Category category = id != null
                    ? categoryService.getCategoryById(id).orElse(new Category())
                    : new Category();
            category.setName(name);
            category.setSlug(slug);
            category.setDescription(description);
            category.setActive(active);
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("message", "Lưu danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("message", "Xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa danh mục này vì có sản phẩm liên quan!");
        }
        return "redirect:/admin/categories";
    }
}
