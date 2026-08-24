package com.siva.shopsphere.products.dto;

import java.util.UUID;

public record ProductCategoryResponse(
    UUID id,
    String name,
    String slug
) {
}
