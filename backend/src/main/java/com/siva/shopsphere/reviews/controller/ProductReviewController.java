package com.siva.shopsphere.reviews.controller;

import com.siva.shopsphere.reviews.dto.CreateReviewRequest;
import com.siva.shopsphere.reviews.dto.ReviewResponse;
import com.siva.shopsphere.reviews.dto.ReviewSummaryResponse;
import com.siva.shopsphere.reviews.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/reviews")
public class ProductReviewController {

    private final ReviewService reviewService;

    public ProductReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviews(productId, page, size));
    }

    @GetMapping("/summary")
    public ResponseEntity<ReviewSummaryResponse> getReviewSummary(@PathVariable UUID productId) {
        return ResponseEntity.ok(reviewService.getReviewSummary(productId));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable UUID productId,
            @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(productId, request));
    }
}
