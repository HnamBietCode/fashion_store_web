package com.fashionstore.fashion_store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String size;  // S, M, L, XL, XXL

    @Column(length = 50)
    private String color;  // Đen, Trắng, Xanh...

    @Column(nullable = false)
    private Integer quantity = 0;  // Số lượng theo variant

    @Column(precision = 12, scale = 0)
    private BigDecimal additionalPrice;  // Giá cộng thêm (nếu có)

    // Relationship: Nhiều Variants thuộc 1 Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}