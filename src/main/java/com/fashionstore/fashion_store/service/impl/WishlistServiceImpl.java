package com.fashionstore.fashion_store.service.impl;

import com.fashionstore.fashion_store.entity.Product;
import com.fashionstore.fashion_store.entity.User;
import com.fashionstore.fashion_store.entity.Wishlist;
import com.fashionstore.fashion_store.repository.ProductRepository;
import com.fashionstore.fashion_store.repository.UserRepository;
import com.fashionstore.fashion_store.repository.WishlistRepository;
import com.fashionstore.fashion_store.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public List<Wishlist> getUserWishlist(Long userId) {
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public boolean toggleWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            wishlistRepository.deleteByUserIdAndProductId(userId, productId);
            return false; // đã xóa
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        wishlistRepository.save(Wishlist.builder().user(user).product(product).build());
        return true; // đã thêm
    }

    @Override
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }
}
