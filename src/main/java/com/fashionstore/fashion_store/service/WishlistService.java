package com.fashionstore.fashion_store.service;

import com.fashionstore.fashion_store.entity.Wishlist;

import java.util.List;

public interface WishlistService {

    List<Wishlist> getUserWishlist(Long userId);

    /** Toggle: thêm vào nếu chưa có, xóa nếu đã có. Trả về true nếu đã thêm */
    boolean toggleWishlist(Long userId, Long productId);

    boolean isInWishlist(Long userId, Long productId);
}
