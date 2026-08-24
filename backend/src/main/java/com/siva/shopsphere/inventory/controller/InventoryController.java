package com.siva.shopsphere.inventory.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.siva.shopsphere.inventory.dto.CreateInventoryRequest;
import com.siva.shopsphere.inventory.dto.InventoryMovementResponse;
import com.siva.shopsphere.inventory.dto.InventoryResponse;
import com.siva.shopsphere.inventory.dto.StockAdjustmentRequest;
import com.siva.shopsphere.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/inventory")
@SecurityRequirement(name = "bearer-jwt")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "Create inventory", description = "ADMIN only")
    @PostMapping
    public ResponseEntity<InventoryResponse> create(@Valid @RequestBody CreateInventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createInventory(request));
    }

    @Operation(summary = "Get inventory for a product", description = "ADMIN only")
    @GetMapping("/products/{productId}")
    public ResponseEntity<InventoryResponse> getByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @Operation(summary = "Adjust stock", description = "ADMIN only")
    @PostMapping("/products/{productId}/adjust")
    public ResponseEntity<InventoryResponse> adjust(@PathVariable UUID productId, @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.adjustStock(productId, request));
    }

    @Operation(summary = "Get movement history", description = "ADMIN only")
    @GetMapping("/products/{productId}/movements")
    public ResponseEntity<List<InventoryMovementResponse>> movements(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getMovementHistory(productId));
    }

    @Operation(summary = "Get low-stock inventory", description = "ADMIN only")
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> lowStock() {
        return ResponseEntity.ok(inventoryService.getLowStockInventory());
    }
}
