package com.siva.shopsphere.products.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
    @NotBlank @Size(max = 150) String name,
    @Size(max = 2000) String description,
    @NotNull @Positive BigDecimal price,
    @NotNull UUID categoryId,
    @NotNull UUID brandId,
    @Size(max = 500) String imageUrl,
    Boolean active
) {
}
