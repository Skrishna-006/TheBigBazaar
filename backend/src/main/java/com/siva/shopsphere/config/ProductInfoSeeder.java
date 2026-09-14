package com.siva.shopsphere.config;

import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.reviews.entity.ProductReview;
import com.siva.shopsphere.reviews.repository.ProductReviewRepository;
import com.siva.shopsphere.sellers.entity.Seller;
import com.siva.shopsphere.sellers.repository.SellerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductInfoSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final ProductReviewRepository reviewRepository;

    public ProductInfoSeeder(ProductRepository productRepository, SellerRepository sellerRepository,
                             ProductReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Ensure sellers exist
        Seller techHub = getOrCreateSeller("TechHub India", "https://ui-avatars.com/api/?name=TechHub+India&background=random", new BigDecimal("4.6"), 1530, 155, "Premium electronics seller");
        Seller audioWorld = getOrCreateSeller("Audio World", "https://ui-avatars.com/api/?name=Audio+World&background=random", new BigDecimal("4.8"), 300, 50, "Top audio gear");
        Seller appleStore = getOrCreateSeller("Apple Authorized", "https://ui-avatars.com/api/?name=Apple+Authorized&background=random", new BigDecimal("4.9"), 10000, 5000, "Official Apple products");

        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            boolean updated = false;

            // Set Seller if missing
            if (p.getSeller() == null) {
                if (p.getBrand().getName().toLowerCase().contains("apple")) {
                    p.setSeller(appleStore);
                } else if (p.getCategory().getName().toLowerCase().contains("audio") || p.getName().toLowerCase().contains("buds") || p.getName().toLowerCase().contains("speaker")) {
                    p.setSeller(audioWorld);
                } else {
                    p.setSeller(techHub);
                }
                updated = true;
            }

            // Highlights and specs are now handled in the JSON columns and already seeded via SQL.

            // Seed Reviews if missing
            long reviewCount = reviewRepository.countByProductIdAndActiveTrue(p.getId());
            if (reviewCount == 0) {
                seedReviewsForProduct(p);
                updated = true;
                
                // Calculate rating synchronously to ensure data integrity
                List<ProductReview> reviews = reviewRepository.findByProductIdAndActiveTrue(p.getId());
                if (!reviews.isEmpty()) {
                    double average = reviews.stream().mapToInt(ProductReview::getRating).average().orElse(0.0);
                    p.setRating(BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP));
                    p.setReviewCount(reviews.size());
                }
            }

            if (updated) {
                productRepository.save(p);
            }
        }
    }

    private Seller getOrCreateSeller(String name, String logo, BigDecimal rating, int count, int followers, String desc) {
        return sellerRepository.findAll().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    Seller s = new Seller();
                    s.setName(name);
                    s.setLogoUrl(logo);
                    s.setRating(rating);
                    s.setRatingCount(count);
                    s.setFollowerCount(followers);
                    s.setDescription(desc);
                    return sellerRepository.save(s);
                });
    }



    private void seedReviewsForProduct(Product p) {
        String name = p.getName().toLowerCase();
        if (name.contains("apple")) {
            addReview(p, 5, "Incredible Device", "The build quality is unmatched. Very smooth performance.", "Ramesh K.");
            addReview(p, 4, "Great but expensive", "I love the features but the price is slightly on the higher side.", "Priya M.");
        } else if (name.contains("thinkpad")) {
            addReview(p, 5, "Best for programming", "The keyboard is an absolute joy to type on. Great for coding.", "Amit Patel");
            addReview(p, 5, "Solid workstation", "Never overheats, great battery life.", "Sarah J.");
        } else if (name.contains("sony") || name.contains("buds")) {
            addReview(p, 5, "Amazing Sound", "The noise cancellation is magical. Very comfortable.", "David W.");
            addReview(p, 4, "Good bass", "Punchy bass and clear vocals. Battery could be slightly better.", "Neha R.");
        } else {
            addReview(p, 5, "Highly recommended", "Exceeded my expectations entirely. Great value for money.", "John D.");
            addReview(p, 4, "Solid purchase", "Does exactly what it says. No complaints.", "Jane Smith");
        }
    }



    private void addReview(Product p, int rating, String title, String text, String name) {
        ProductReview r = new ProductReview();
        r.setProductId(p.getId());
        r.setRating(rating);
        r.setTitle(title);
        r.setReviewText(text);
        r.setReviewerName(name);
        r.setVerifiedPurchase(false);
        r.setActive(true);
        reviewRepository.save(r);
    }
}
