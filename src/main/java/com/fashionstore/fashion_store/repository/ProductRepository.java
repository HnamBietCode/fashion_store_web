package com.fashionstore.fashion_store.repository;

import com.fashionstore.fashion_store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    // Tìm sản phẩm theo category
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    // Sản phẩm nổi bật
    List<Product> findByFeaturedTrueAndActiveTrue();

    // Tìm kiếm theo tên (không phân biệt hoa thường)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.active = true")
    Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable);

    // Sản phẩm mới nhất
    List<Product> findTop8ByActiveTrueOrderByCreatedAtDesc();
}