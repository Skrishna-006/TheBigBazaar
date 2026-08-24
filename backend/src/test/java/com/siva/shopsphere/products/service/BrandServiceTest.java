package com.siva.shopsphere.products.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.products.dto.CreateBrandRequest;
import com.siva.shopsphere.products.dto.UpdateBrandRequest;
import com.siva.shopsphere.products.entity.Brand;
import com.siva.shopsphere.products.repository.BrandRepository;
import com.siva.shopsphere.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private BrandService brandService;

    @Test
    void createBrandGeneratesSlugAndActivatesBrand() {
        when(brandRepository.findByNameIgnoreCase("Apple")).thenReturn(Optional.empty());
        when(brandRepository.findBySlug("apple")).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = brandService.createBrand(new CreateBrandRequest("Apple", "Phones", "https://example.com/apple.jpg"));

        assertThat(response.slug()).isEqualTo("apple");
        assertThat(response.active()).isTrue();
    }

    @Test
    void duplicateNameIsRejected() {
        Brand existing = new Brand();
        existing.setId(UUID.randomUUID());
        when(brandRepository.findByNameIgnoreCase("Nike")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> brandService.createBrand(new CreateBrandRequest("Nike", null, null)))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void duplicateSlugIsRejected() {
        Brand existing = new Brand();
        existing.setId(UUID.randomUUID());
        when(brandRepository.findByNameIgnoreCase("Home Brand")).thenReturn(Optional.empty());
        when(brandRepository.findBySlug("home-brand")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> brandService.createBrand(new CreateBrandRequest("Home Brand", null, null)))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void listReturnsActiveBrandsOnly() {
        Brand brand = new Brand();
        brand.setId(UUID.randomUUID());
        brand.setName("Samsung");
        brand.setSlug("samsung");
        brand.setActive(true);
        when(brandRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(brand));

        assertThat(brandService.getActiveBrands()).hasSize(1);
    }

    @Test
    void updateRegeneratesSlugWhenNameChanges() {
        Brand brand = new Brand();
        brand.setId(UUID.randomUUID());
        brand.setName("Apple");
        brand.setSlug("apple");
        brand.setActive(true);
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));
        when(brandRepository.findByNameIgnoreCase("Apple Inc")).thenReturn(Optional.empty());
        when(brandRepository.findBySlug("apple-inc")).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = brandService.updateBrand(brand.getId(), new UpdateBrandRequest("Apple Inc", "Updated", null, true));

        assertThat(response.slug()).isEqualTo("apple-inc");
    }

    @Test
    void deactivateBrandSetsActiveFalse() {
        Brand brand = new Brand();
        brand.setId(UUID.randomUUID());
        brand.setActive(true);
        when(brandRepository.findById(brand.getId())).thenReturn(Optional.of(brand));
        when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

        brandService.deactivateBrand(brand.getId());

        verify(brandRepository).save(brand);
        assertThat(brand.isActive()).isFalse();
    }
}
