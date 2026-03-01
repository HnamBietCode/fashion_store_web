package com.fashionstore.fashion_store.service.impl;

import com.fashionstore.fashion_store.entity.Coupon;
import com.fashionstore.fashion_store.repository.CouponRepository;
import com.fashionstore.fashion_store.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return couponRepository.findByCodeIgnoreCase(code);
    }

    @Override
    public BigDecimal validateAndCalculate(String code, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));

        if (!coupon.isActive()) {
            throw new RuntimeException("Mã giảm giá đã bị vô hiệu hóa!");
        }
        if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn!");
        }
        if (coupon.getUsageLimit() != null
                && coupon.getUsedCount() != null
                && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
        }
        if (coupon.getMinOrderAmount() != null
                && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new RuntimeException(
                    "Đơn hàng tối thiểu " + coupon.getMinOrderAmount().longValue() + "đ để dùng mã này!");
        }
        return coupon.calculateDiscount(orderAmount);
    }

    @Override
    public Coupon save(Coupon coupon) {
        // Chuẩn hóa code thành chữ HOA
        coupon.setCode(coupon.getCode().toUpperCase().trim());
        return couponRepository.save(coupon);
    }

    @Override
    public void delete(Long id) {
        couponRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void incrementUsage(String code) {
        couponRepository.findByCodeIgnoreCase(code).ifPresent(c -> {
            c.setUsedCount((c.getUsedCount() == null ? 0 : c.getUsedCount()) + 1);
            couponRepository.save(c);
        });
    }
}
