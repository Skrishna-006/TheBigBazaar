package com.siva.shopsphere.products.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ForbiddenException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.products.dto.CreateProductRequest;
import com.siva.shopsphere.products.dto.ProductResponse;
import com.siva.shopsphere.products.dto.UpdateProductRequest;
import com.siva.shopsphere.products.entity.Brand;
import com.siva.shopsphere.products.entity.Category;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.mapper.ProductMapper;
import com.siva.shopsphere.products.repository.BrandRepository;
import com.siva.shopsphere.products.repository.CategoryRepository;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.products.util.SlugUtils;
import com.siva.shopsphere.security.CurrentUserService;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final CurrentUserService currentUserService;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, BrandRepository brandRepository, CurrentUserService currentUserService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        requireAdmin();
        ensureSkuAvailable(request.sku(), null);
        String slug = SlugUtils.toSlug(request.name().trim());
        ensureSlugAvailable(slug, null);
        Category category = loadActiveCategory(request.categoryId());
        Brand brand = loadActiveBrand(request.brandId());

        Product product = ProductMapper.toEntity(request);
        product.setName(request.name().trim());
        product.setSlug(slug);
        product.setSku(request.sku().trim());
        product.setCategory(category);
        product.setBrand(brand);
        product.setActive(true);
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts(UUID categoryId, UUID brandId) {
        List<Product> products;
        if (categoryId != null && brandId != null) {
            products = productRepository.findAllByActiveTrueAndCategory_IdAndBrand_IdOrderByNameAsc(categoryId, brandId);
        } else if (categoryId != null) {
            products = productRepository.findAllByActiveTrueAndCategory_IdOrderByNameAsc(categoryId);
        } else if (brandId != null) {
            products = productRepository.findAllByActiveTrueAndBrand_IdOrderByNameAsc(brandId);
        } else {
            products = productRepository.findAllByActiveTrueOrderByNameAsc();
        }
        return products.stream().map(ProductMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
            .filter(Product::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return ProductMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
            .filter(Product::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return ProductMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        requireAdmin();
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Category category = loadActiveCategory(request.categoryId());
        Brand brand = loadActiveBrand(request.brandId());
        ensureSkuAvailable(product.getSku(), product.getId());
        String slug = SlugUtils.toSlug(request.name().trim());
        ensureSlugAvailable(slug, product.getId());

        ProductMapper.updateEntity(product, request);
        product.setName(request.name().trim());
        product.setSlug(slug);
        product.setCategory(category);
        product.setBrand(brand);
        if (request.active() != null) {
            product.setActive(request.active());
        }
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void deactivateProduct(UUID id) {
        requireAdmin();
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    private Category loadActiveCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.isActive()) {
            throw new ResourceNotFoundException("Category not found");
        }
        return category;
    }

    private Brand loadActiveBrand(UUID brandId) {
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        if (!brand.isActive()) {
            throw new ResourceNotFoundException("Brand not found");
        }
        return brand;
    }

    private void ensureSkuAvailable(String sku, UUID currentId) {
        productRepository.findBySku(sku.trim()).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ConflictException("Product with this SKU already exists");
            }
        });
    }

    private void ensureSlugAvailable(String slug, UUID currentId) {
        productRepository.findBySlug(slug).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ConflictException("Product with this slug already exists");
            }
        });
    }

    private void requireAdmin() {
        if (currentUserService.getCurrentUser().getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Access denied");
        }
    }
}
