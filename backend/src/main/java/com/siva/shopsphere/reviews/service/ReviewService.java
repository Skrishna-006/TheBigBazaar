package com.siva.shopsphere.reviews.service;

import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.orders.repository.OrderRepository;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.reviews.dto.CreateReviewRequest;
import com.siva.shopsphere.reviews.dto.ReviewResponse;
import com.siva.shopsphere.reviews.dto.ReviewSummaryResponse;
import com.siva.shopsphere.reviews.entity.ProductReview;
import com.siva.shopsphere.reviews.repository.ProductReviewRepository;
import com.siva.shopsphere.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final OrderRepository orderRepository;

    public ReviewService(ProductReviewRepository reviewRepository, ProductRepository productRepository, CurrentUserService currentUserService, OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviews(UUID productId, int page, int size) {
        return reviewRepository.findByProductIdAndActiveTrueOrderByCreatedAtDesc(productId, PageRequest.of(page, size))
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getReviewSummary(UUID productId) {
        List<ProductReview> reviews = reviewRepository.findByProductIdAndActiveTrue(productId);
        long totalReviews = reviews.size();
        if (totalReviews == 0) {
            return new ReviewSummaryResponse(BigDecimal.ZERO, 0L, Map.of(1, 0L, 2, 0L, 3, 0L, 4, 0L, 5, 0L));
        }

        double average = reviews.stream().mapToInt(ProductReview::getRating).average().orElse(0.0);
        BigDecimal avgRating = BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);

        Map<Integer, Long> distribution = reviews.stream()
            .collect(Collectors.groupingBy(ProductReview::getRating, Collectors.counting()));
        
        for (int i = 1; i <= 5; i++) {
            distribution.putIfAbsent(i, 0L);
        }

        return new ReviewSummaryResponse(avgRating, totalReviews, distribution);
    }

    @Transactional
    public ReviewResponse createReview(UUID productId, CreateReviewRequest request) {
        User user = currentUserService.getCurrentUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to write a review.");
        }
        
        if (reviewRepository.findByProductIdAndUserId(productId, user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this product.");
        }

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductReview review = new ProductReview();
        review.setProductId(productId);
        review.setUserId(user.getId());
        review.setReviewerName(user.getFirstName() + " " + user.getLastName());
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setReviewText(request.reviewText());
        
        boolean verified = orderRepository.existsByUserIdAndItemsProductId(user.getId(), productId);
        review.setVerifiedPurchase(verified);
        review.setActive(true);

        review = reviewRepository.save(review);
        updateProductRating(product);

        return mapToResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(UUID reviewId, CreateReviewRequest request) {
        User user = currentUserService.getCurrentUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to write a review.");
        }

        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own review.");
        }

        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setReviewText(request.reviewText());

        review = reviewRepository.save(review);
        
        Product product = productRepository.findById(review.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        updateProductRating(product);

        return mapToResponse(review);
    }

    @Transactional
    public void deleteReview(UUID reviewId) {
        User user = currentUserService.getCurrentUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to write a review.");
        }

        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only modify your own review.");
        }

        reviewRepository.delete(review);

        Product product = productRepository.findById(review.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        updateProductRating(product);
    }

    private void updateProductRating(Product product) {
        ReviewSummaryResponse summary = getReviewSummary(product.getId());
        product.setRating(summary.averageRating());
        product.setReviewCount((int) summary.totalReviews());
        productRepository.save(product);
    }

    private ReviewResponse mapToResponse(ProductReview review) {
        return new ReviewResponse(
            review.getId(),
            review.getProductId(),
            review.getRating(),
            review.getTitle(),
            review.getReviewText(),
            review.getReviewerName(),
            review.isVerifiedPurchase(),
            review.getHelpfulCount(),
            review.getCreatedAt()
        );
    }
}
