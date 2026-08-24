package com.siva.shopsphere.products.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siva.shopsphere.products.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySku(String sku);
    boolean existsBySlug(String slug);
    boolean existsBySku(String sku);
    List<Product> findAllByActiveTrueOrderByNameAsc();
    List<Product> findAllByActiveTrueAndCategory_IdOrderByNameAsc(UUID categoryId);
    List<Product> findAllByActiveTrueAndBrand_IdOrderByNameAsc(UUID brandId);
    List<Product> findAllByActiveTrueAndCategory_IdAndBrand_IdOrderByNameAsc(UUID categoryId, UUID brandId);
}
