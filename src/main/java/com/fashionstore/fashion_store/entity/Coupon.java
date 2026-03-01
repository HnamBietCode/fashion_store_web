package com.fashionstore.fashion_store.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code; // VD: SALE20, FREESHIP

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // PERCENT hoặc FIXED

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue; // 20 (%) hoặc 50000 (đ)

    @Column(precision = 12, scale = 2)
    private BigDecimal minOrderAmount; // đơn tối thiểu, null = không giới hạn

    @Column(precision = 12, scale = 2)
    private BigDecimal maxDiscount; // giảm tối đa (cho PERCENT), null = không giới hạn

    private Integer usageLimit; // số lần dùng tối đa, null = không giới hạn
    private Integer usedCount; // đã dùng bao nhiêu lần

    private LocalDateTime expiresAt; // null = không hết hạn

    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum DiscountType {
        PERCENT, FIXED
    }

    /** Tính số tiền giảm thực tế */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (discountType == DiscountType.PERCENT) {
            BigDecimal discount = orderAmount.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100));
            if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
                discount = maxDiscount;
            }
            return discount;
        } else {
            return discountValue.min(orderAmount);
        }
    }
}
