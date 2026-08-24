package com.siva.shopsphere.products.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.products.dto.BrandResponse;
import com.siva.shopsphere.products.dto.CreateBrandRequest;
import com.siva.shopsphere.products.dto.UpdateBrandRequest;
import com.siva.shopsphere.products.entity.Brand;
import com.siva.shopsphere.products.mapper.BrandMapper;
import com.siva.shopsphere.products.repository.BrandRepository;
import com.siva.shopsphere.products.util.SlugUtils;
import com.siva.shopsphere.security.CurrentUserService;

@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final CurrentUserService currentUserService;

    public BrandService(BrandRepository brandRepository, CurrentUserService currentUserService) {
        this.brandRepository = brandRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public BrandResponse createBrand(CreateBrandRequest request) {
        String name = normalizeName(request.name());
        ensureNameAndSlugAvailable(name, SlugUtils.toSlug(name), null);
        Brand brand = BrandMapper.toEntity(request);
        brand.setName(name);
        brand.setSlug(SlugUtils.toSlug(name));
        brand.setActive(true);
        return BrandMapper.toResponse(brandRepository.save(brand));
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getActiveBrands() {
        return brandRepository.findAllByActiveTrueOrderByNameAsc().stream()
            .map(BrandMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse getBrandById(UUID id) {
        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        if (!brand.isActive() && !isAdmin()) {
            throw new ResourceNotFoundException("Brand not found");
        }
        return BrandMapper.toResponse(brand);
    }

    @Transactional
    public BrandResponse updateBrand(UUID id, UpdateBrandRequest request) {
        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        String name = normalizeName(request.name());
        String slug = SlugUtils.toSlug(name);
        ensureNameAndSlugAvailable(name, slug, brand.getId());
        BrandMapper.updateEntity(brand, request);
        brand.setName(name);
        brand.setSlug(slug);
        if (request.active() != null) {
            brand.setActive(request.active());
        }
        return BrandMapper.toResponse(brandRepository.save(brand));
    }

    @Transactional
    public void deactivateBrand(UUID id) {
        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        brand.setActive(false);
        brandRepository.save(brand);
    }

    private void ensureNameAndSlugAvailable(String name, String slug, UUID currentId) {
        brandRepository.findByNameIgnoreCase(name).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ConflictException("Brand with this name already exists");
            }
        });
        brandRepository.findBySlug(slug).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new ConflictException("Brand with this slug already exists");
            }
        });
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private boolean isAdmin() {
        try {
            return currentUserService.getCurrentUser().getRole() == UserRole.ADMIN;
        } catch (ResourceNotFoundException ex) {
            return false;
        }
    }
}
