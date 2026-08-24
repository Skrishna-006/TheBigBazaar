package com.siva.shopsphere.products.dto;

import java.util.UUID;

public record ProductBrandResponse(
    UUID id,
    String name,
    String slug
) {
}
