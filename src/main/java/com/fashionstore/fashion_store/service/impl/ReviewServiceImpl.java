package com.fashionstore.fashion_store.service.impl;

import com.fashionstore.fashion_store.entity.Product;
import com.fashionstore.fashion_store.entity.Review;
import com.fashionstore.fashion_store.entity.User;
import com.fashionstore.fashion_store.repository.ProductRepository;
import com.fashionstore.fashion_store.repository.ReviewRepository;
import com.fashionstore.fashion_store.repository.UserRepository;
import com.fashionstore.fashion_store.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    public Optional<Review> getUserReview(Long userId, Long productId) {
        return reviewRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean hasUserReviewed(Long userId, Long productId) {
        return reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional
    public Review addReview(Long userId, Long productId, int rating, String comment) {
        // Nếu đã review rồi thì cập nhật
        Optional<Review> existing = reviewRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            Review r = existing.get();
            r.setRating(rating);
            r.setComment(comment);
            return reviewRepository.save(r);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .comment(comment)
                .build();
        return reviewRepository.save(review);
    }

    @Override
    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.avgRatingByProductId(productId);
        return avg != null ? avg : 0.0;
    }

    @Override
    public long getReviewCount(Long productId) {
        return reviewRepository.countByProductId(productId);
    }
}
