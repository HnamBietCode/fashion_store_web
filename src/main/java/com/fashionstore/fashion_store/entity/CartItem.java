package com.fashionstore.fashion_store.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant; // Optional - nếu chọn size/màu

    @Column(nullable = false)
    private Integer quantity = 1;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // Tính giá của item này
    public BigDecimal getSubtotal() {
        BigDecimal price = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();
        if (variant != null && variant.getAdditionalPrice() != null) {
            price = price.add(variant.getAdditionalPrice());
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}