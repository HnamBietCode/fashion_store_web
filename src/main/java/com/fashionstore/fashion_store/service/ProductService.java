package com.fashionstore.fashion_store.service;

import com.fashionstore.fashion_store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Page<Product> getAllProducts(Pageable pageable);

    Optional<Product> getProductById(Long id);

    Optional<Product> getProductBySlug(String slug);

    Page<Product> getProductsByCategory(Long categoryId, Pageable pageable);

    Page<Product> searchProducts(String keyword, Pageable pageable);

    List<Product> getFeaturedProducts();

    List<Product> getLatestProducts();

    Product saveProduct(Product product);

    void deleteProduct(Long id);
}