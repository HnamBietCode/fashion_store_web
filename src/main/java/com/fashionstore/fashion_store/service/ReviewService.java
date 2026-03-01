package com.fashionstore.fashion_store.service;

import com.fashionstore.fashion_store.entity.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewService {

    List<Review> getProductReviews(Long productId);

    Optional<Review> getUserReview(Long userId, Long productId);

    boolean hasUserReviewed(Long userId, Long productId);

    Review addReview(Long userId, Long productId, int rating, String comment);

    Double getAverageRating(Long productId);

    long getReviewCount(Long productId);
}
