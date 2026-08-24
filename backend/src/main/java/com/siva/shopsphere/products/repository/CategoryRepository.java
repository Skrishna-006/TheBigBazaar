package com.siva.shopsphere.products.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siva.shopsphere.products.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Category> findAllByActiveTrueOrderByNameAsc();
}
