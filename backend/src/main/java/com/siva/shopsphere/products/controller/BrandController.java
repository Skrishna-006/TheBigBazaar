package com.siva.shopsphere.products.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.siva.shopsphere.products.dto.BrandResponse;
import com.siva.shopsphere.products.dto.CreateBrandRequest;
import com.siva.shopsphere.products.dto.UpdateBrandRequest;
import com.siva.shopsphere.products.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/brands")
@SecurityRequirement(name = "bearer-jwt")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @Operation(summary = "Create a brand", description = "ADMIN only")
    @PostMapping
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody CreateBrandRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.createBrand(request));
    }

    @Operation(summary = "List active brands")
    @GetMapping
    public ResponseEntity<List<BrandResponse>> list() {
        return ResponseEntity.ok(brandService.getActiveBrands());
    }

    @Operation(summary = "Get an active brand by id")
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @Operation(summary = "Update a brand", description = "ADMIN only")
    @PutMapping("/{id}")
    public ResponseEntity<BrandResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateBrandRequest request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    @Operation(summary = "Deactivate a brand", description = "ADMIN only")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        brandService.deactivateBrand(id);
        return ResponseEntity.noContent().build();
    }
}
