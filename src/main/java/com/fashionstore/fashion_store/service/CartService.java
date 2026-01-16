package com.fashionstore.fashion_store.service;

import com.fashionstore.fashion_store.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {

    List<CartItem> getCartItems(Long userId);

    CartItem addToCart(Long userId, Long productId, Long variantId, Integer quantity);

    CartItem updateQuantity(Long cartItemId, Integer quantity);

    void removeFromCart(Long cartItemId);

    void clearCart(Long userId);

    int getCartItemCount(Long userId);

    BigDecimal getCartTotal(Long userId);
}