package com.siva.shopsphere.reviews.repository;

import com.siva.shopsphere.reviews.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    Page<ProductReview> findByProductIdAndActiveTrueOrderByCreatedAtDesc(UUID productId, Pageable pageable);
    List<ProductReview> findByProductIdAndActiveTrue(UUID productId);
    long countByProductIdAndActiveTrue(UUID productId);
    
    java.util.Optional<ProductReview> findByProductIdAndUserId(UUID productId, UUID userId);
}
