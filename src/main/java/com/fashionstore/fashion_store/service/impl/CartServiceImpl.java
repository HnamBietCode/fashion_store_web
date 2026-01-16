package com.fashionstore.fashion_store.service.impl;

import com.fashionstore.fashion_store.entity.CartItem;
import com.fashionstore.fashion_store.entity.Product;
import com.fashionstore.fashion_store.entity.ProductVariant;
import com.fashionstore.fashion_store.entity.User;
import com.fashionstore.fashion_store.repository.CartItemRepository;
import com.fashionstore.fashion_store.repository.ProductRepository;
import com.fashionstore.fashion_store.repository.ProductVariantRepository;
import com.fashionstore.fashion_store.repository.UserRepository;
import com.fashionstore.fashion_store.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    @Override
    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Override
    public CartItem addToCart(Long userId, Long productId, Long variantId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Kiểm tra đã có trong giỏ chưa
        CartItem existingItem;
        if (variantId != null) {
            existingItem = cartItemRepository.findByUserIdAndProductIdAndVariantId(userId, productId, variantId)
                    .orElse(null);
        } else {
            existingItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                    .orElse(null);
        }

        if (existingItem != null) {
            // Cập nhật số lượng
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            return cartItemRepository.save(existingItem);
        }

        // Tạo mới
        CartItem.CartItemBuilder builder = CartItem.builder()
                .user(user)
                .product(product)
                .quantity(quantity);

        if (variantId != null) {
            ProductVariant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Variant not found"));
            builder.variant(variant);
        }

        return cartItemRepository.save(builder.build());
    }

    @Override
    public CartItem updateQuantity(Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Override
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Override
    public int getCartItemCount(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }

    @Override
    public BigDecimal getCartTotal(Long userId) {
        return getCartItems(userId).stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}