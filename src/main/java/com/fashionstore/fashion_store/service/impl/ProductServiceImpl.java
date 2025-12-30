package com.fashionstore.fashion_store.service.impl;

import com.fashionstore.fashion_store.entity.Product;
import com.fashionstore.fashion_store.repository.ProductRepository;
import com.fashionstore.fashion_store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    @Override
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
    }

    @Override
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByName(keyword, pageable);
    }

    @Override
    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue();
    }

    @Override
    public List<Product> getLatestProducts() {
        return productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}