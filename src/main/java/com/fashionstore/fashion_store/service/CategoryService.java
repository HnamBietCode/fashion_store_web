package com.fashionstore.fashion_store.service;

import com.fashionstore.fashion_store.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    List<Category> getAllCategories();

    List<Category> getAllActiveCategories();

    Optional<Category> getCategoryById(Long id);

    Optional<Category> getCategoryBySlug(String slug);

    Category saveCategory(Category category);

    void deleteCategory(Long id);
}