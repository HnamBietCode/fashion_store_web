package com.fashionstore.fashion_store.service.impl;

import com.fashionstore.fashion_store.entity.Category;
import com.fashionstore.fashion_store.entity.Product;
import com.fashionstore.fashion_store.repository.CategoryRepository;
import com.fashionstore.fashion_store.repository.ProductRepository;
import com.fashionstore.fashion_store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import com.fashionstore.fashion_store.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

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
    public Product saveProduct(Long id, String name, String slug, String description,
            BigDecimal price, BigDecimal salePrice, String imageUrl,
            Integer stockQuantity, Long categoryId, boolean featured, boolean active) {
        // COLLECTIONS: Stream API để tìm category — functional style
        // orElseThrow là Optional method — tránh NullPointerException
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        Product product;
        if (id != null) {
            // Optional.orElseThrow() — EXCEPTION HANDLING với Optional
            // Tránh dùng .get() vì sẽ throw NoSuchElementException không rõ nghĩa
            product = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        } else {
            product = new Product();
        }

        product.setName(name);
        product.setSlug(slug);
        product.setDescription(description);
        product.setPrice(price);
        product.setSalePrice(salePrice);
        product.setImageUrl(imageUrl);
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        product.setFeatured(featured);
        product.setActive(active);

        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}