package com.fashionstore.fashion_store.service;

import com.fashionstore.fashion_store.entity.Coupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CouponService {

    List<Coupon> getAllCoupons();

    Optional<Coupon> findByCode(String code);

    /**
     * Validate mã giảm giá và trả về số tiền giảm.
     * Throws RuntimeException nếu không hợp lệ.
     */
    BigDecimal validateAndCalculate(String code, BigDecimal orderAmount);

    Coupon save(Coupon coupon);

    void delete(Long id);

    void incrementUsage(String code);
}
