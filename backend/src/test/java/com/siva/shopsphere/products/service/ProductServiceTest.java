package com.siva.shopsphere.products.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.products.dto.CreateProductRequest;
import com.siva.shopsphere.products.dto.UpdateProductRequest;
import com.siva.shopsphere.products.entity.Brand;
import com.siva.shopsphere.products.entity.Category;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.BrandRepository;
import com.siva.shopsphere.products.repository.CategoryRepository;
import com.siva.shopsphere.products.repository.ProductRepository;
import com.siva.shopsphere.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ProductService productService;

    private User admin;
    private Category category;
    private Brand brand;
    private Product product;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setEmail("admin@example.com");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        when(currentUserService.getCurrentUser()).thenReturn(admin);

        category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setActive(true);

        brand = new Brand();
        brand.setId(UUID.randomUUID());
        brand.setName("Apple");
        brand.setSlug("apple");
        brand.setActive(true);

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("MacBook Air M3");
        product.setSlug("macbook-air-m3");
        product.setSku("MBA-M3-256");
        product.setPrice(new BigDecimal("99999.00"));
        product.setCategory(category);
        product.setBrand(brand);
        product.setActive(true);
    }

    @Test
    void createProductGeneratesSlugAndSaves() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));
        when(productRepository.findBySku("MBA-M3-256")).thenReturn(Optional.empty());
        when(productRepository.findBySlug("macbook-air-m3")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = productService.createProduct(new CreateProductRequest(
            "MacBook Air M3",
            "MBA-M3-256",
            "Apple MacBook Air with M3 chip",
            new BigDecimal("99999.00"),
            null,
            null,
            null,
            null,
            null,
            category.getId(),
            brand.getId(),
            "https://example.com/macbook.jpg"
        ));

        assertThat(response.slug()).isEqualTo("macbook-air-m3");
        assertThat(response.sku()).isEqualTo("MBA-M3-256");
    }

    @Test
    void createProductRejectsOriginalPriceLowerThanSellingPrice() {
        assertThatThrownBy(() -> productService.createProduct(new CreateProductRequest(
            "MacBook Air M3",
            "MBA-M3-256",
            "Desc",
            new BigDecimal("100.00"),
            new BigDecimal("90.00"), // Lower than price
            null, null, null, null,
            category.getId(), brand.getId(), null
        ))).isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Original price must be greater than or equal to the selling price");
    }

    @Test
    void duplicateSkuIsRejected() {
        when(productRepository.findBySku("MBA-M3-256")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.createProduct(new CreateProductRequest(
            "MacBook Air M3",
            "MBA-M3-256",
            "Apple MacBook Air with M3 chip",
            new BigDecimal("99999.00"),
            null, null, null, null, null,
            category.getId(),
            brand.getId(),
            null
        ))).isInstanceOf(ConflictException.class);
    }

    @Test
    void duplicateSlugIsRejected() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));
        when(productRepository.findBySku("MBA-M3-256")).thenReturn(Optional.empty());
        when(productRepository.findBySlug("macbook-air-m3")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.createProduct(new CreateProductRequest(
            "MacBook Air M3",
            "MBA-M3-256",
            "Apple MacBook Air with M3 chip",
            new BigDecimal("99999.00"),
            null, null, null, null, null,
            category.getId(),
            brand.getId(),
            null
        ))).isInstanceOf(ConflictException.class);
    }

    @Test
    void getActiveProductsReturnsOnlyActiveProducts() {
        when(productRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(product));
        assertThat(productService.getActiveProducts(null, null)).hasSize(1);
    }

    @Test
    void getProductByIdReturnsActiveProduct() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        assertThat(productService.getProductById(product.getId()).sku()).isEqualTo("MBA-M3-256");
    }

    @Test
    void updateProductRegeneratesSlugAndDoesNotChangeSku() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));
        when(productRepository.findBySku("MBA-M3-256")).thenReturn(Optional.of(product));
        when(productRepository.findBySlug("macbook-air-m3-pro")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = productService.updateProduct(product.getId(), new UpdateProductRequest(
            "MacBook Air M3 Pro",
            "Updated description",
            new BigDecimal("109999.00"),
            null, null, null, null, null,
            category.getId(),
            brand.getId(),
            "https://example.com/macbook-pro.jpg",
            true
        ));

        assertThat(response.slug()).isEqualTo("macbook-air-m3-pro");
        assertThat(response.sku()).isEqualTo("MBA-M3-256");
    }

    @Test
    void updateProductRejectsOriginalPriceLowerThanSellingPrice() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        
        assertThatThrownBy(() -> productService.updateProduct(product.getId(), new UpdateProductRequest(
            "MacBook Air M3 Pro",
            "Updated description",
            new BigDecimal("100.00"),
            new BigDecimal("90.00"), // Lower than price
            null, null, null, null,
            category.getId(), brand.getId(), null, true
        ))).isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Original price must be greater than or equal to the selling price");
    }

    @Test
    void deactivateProductSetsActiveFalse() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.deactivateProduct(product.getId());

        verify(productRepository).save(product);
        assertThat(product.isActive()).isFalse();
    }
}
