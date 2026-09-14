package com.siva.shopsphere.reviews.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.orders.repository.OrderRepository;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.reviews.dto.CreateReviewRequest;
import com.siva.shopsphere.reviews.dto.ReviewResponse;
import com.siva.shopsphere.reviews.entity.ProductReview;
import com.siva.shopsphere.reviews.repository.ProductReviewRepository;
import com.siva.shopsphere.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ProductReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Product testProduct;
    private ProductReview testReview;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testProduct = new Product();
        testProduct.setId(UUID.randomUUID());

        testReview = new ProductReview();
        testReview.setId(UUID.randomUUID());
        testReview.setProductId(testProduct.getId());
        testReview.setUserId(testUser.getId());
        testReview.setRating(5);
        testReview.setReviewText("Great product!");
    }

    @Test
    void createReview_Success() {
        CreateReviewRequest request = new CreateReviewRequest(5, "Title", "Great product!");
        
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(reviewRepository.findByProductIdAndUserId(testProduct.getId(), testUser.getId()))
            .thenReturn(Optional.empty());
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));
        when(orderRepository.existsByUserIdAndItemsProductId(testUser.getId(), testProduct.getId()))
            .thenReturn(true);
        
        ProductReview savedReview = new ProductReview();
        savedReview.setId(UUID.randomUUID());
        savedReview.setProductId(testProduct.getId());
        savedReview.setUserId(testUser.getId());
        savedReview.setRating(5);
        savedReview.setVerifiedPurchase(true);
        when(reviewRepository.save(any(ProductReview.class))).thenReturn(savedReview);

        ReviewResponse response = reviewService.createReview(testProduct.getId(), request);

        assertNotNull(response);
        assertEquals(5, response.rating());
        assertTrue(response.verifiedPurchase());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createReview_Duplicate_ThrowsException() {
        CreateReviewRequest request = new CreateReviewRequest(5, "Title", "Great product!");
        
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(reviewRepository.findByProductIdAndUserId(testProduct.getId(), testUser.getId()))
            .thenReturn(Optional.of(testReview));

        assertThrows(ResponseStatusException.class, () -> 
            reviewService.createReview(testProduct.getId(), request));
    }

    @Test
    void createReview_Unauthenticated_ThrowsException() {
        CreateReviewRequest request = new CreateReviewRequest(5, "Title", "Great product!");
        
        when(currentUserService.getCurrentUser()).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> 
            reviewService.createReview(testProduct.getId(), request));
    }

    @Test
    void updateReview_Success() {
        CreateReviewRequest request = new CreateReviewRequest(4, "Title", "Updated text");
        
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(ProductReview.class))).thenReturn(testReview);
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        ReviewResponse response = reviewService.updateReview(testReview.getId(), request);

        assertNotNull(response);
        assertEquals(4, response.rating());
        assertEquals("Updated text", response.reviewText());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateReview_WrongUser_ThrowsException() {
        CreateReviewRequest request = new CreateReviewRequest(4, "Title", "Updated text");
        
        User wrongUser = new User();
        wrongUser.setId(UUID.randomUUID());
        
        when(currentUserService.getCurrentUser()).thenReturn(wrongUser);
        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));

        assertThrows(ResponseStatusException.class, () -> 
            reviewService.updateReview(testReview.getId(), request));
    }

    @Test
    void deleteReview_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));
        when(productRepository.findById(testProduct.getId())).thenReturn(Optional.of(testProduct));

        reviewService.deleteReview(testReview.getId());

        verify(reviewRepository, times(1)).delete(testReview);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void deleteReview_WrongUser_ThrowsException() {
        User wrongUser = new User();
        wrongUser.setId(UUID.randomUUID());
        
        when(currentUserService.getCurrentUser()).thenReturn(wrongUser);
        when(reviewRepository.findById(testReview.getId())).thenReturn(Optional.of(testReview));

        assertThrows(ResponseStatusException.class, () -> 
            reviewService.deleteReview(testReview.getId()));
    }
}
