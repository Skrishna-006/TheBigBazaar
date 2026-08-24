package com.siva.shopsphere.products.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siva.shopsphere.products.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, UUID> {
    Optional<Brand> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    Optional<Brand> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Brand> findAllByActiveTrueOrderByNameAsc();
}
