package com.fashionstore.fashion_store.controller;

import com.fashionstore.fashion_store.entity.Product;
import com.fashionstore.fashion_store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductSearchApiController {

    private final ProductRepository productRepository;

    /**
     * API tìm kiếm nhanh — trả về JSON cho autocomplete navbar
     * GET /api/products/search?q=ao&limit=8
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "8") int limit) {

        if (q == null || q.trim().length() < 1) {
            return ResponseEntity.ok(List.of());
        }

        var pageable = PageRequest.of(0, limit);
        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(q.trim(), pageable);

        List<Map<String, Object>> result = products.stream().map(p -> Map.<String, Object>of(
                "id", p.getId(),
                "name", p.getName(),
                "slug", p.getSlug(),
                "price", p.getSalePrice() != null ? p.getSalePrice() : p.getPrice(),
                "image", p.getImageUrl() != null ? p.getImageUrl() : "")).toList();

        return ResponseEntity.ok(result);
    }
}
