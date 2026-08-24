package com.siva.shopsphere.products.mapper;

import com.siva.shopsphere.products.dto.BrandResponse;
import com.siva.shopsphere.products.dto.CreateBrandRequest;
import com.siva.shopsphere.products.dto.UpdateBrandRequest;
import com.siva.shopsphere.products.entity.Brand;

public final class BrandMapper {
    private BrandMapper() {
    }

    public static Brand toEntity(CreateBrandRequest request) {
        Brand brand = new Brand();
        apply(brand, request.name(), request.description(), request.logoUrl(), null);
        return brand;
    }

    public static void updateEntity(Brand brand, UpdateBrandRequest request) {
        apply(brand, request.name(), request.description(), request.logoUrl(), request.active());
    }

    public static BrandResponse toResponse(Brand brand) {
        return new BrandResponse(
            brand.getId(),
            brand.getName(),
            brand.getSlug(),
            brand.getDescription(),
            brand.getLogoUrl(),
            brand.isActive(),
            brand.getCreatedAt(),
            brand.getUpdatedAt()
        );
    }

    private static void apply(Brand brand, String name, String description, String logoUrl, Boolean active) {
        if (name != null) {
            brand.setName(name.trim());
        }
        brand.setDescription(description == null ? null : description.trim());
        brand.setLogoUrl(logoUrl == null ? null : logoUrl.trim());
        if (active != null) {
            brand.setActive(active);
        }
    }
}
