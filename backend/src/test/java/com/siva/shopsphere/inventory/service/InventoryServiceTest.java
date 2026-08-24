package com.siva.shopsphere.inventory.service;

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

import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.inventory.dto.CreateInventoryRequest;
import com.siva.shopsphere.inventory.dto.StockAdjustmentRequest;
import com.siva.shopsphere.inventory.entity.Inventory;
import com.siva.shopsphere.inventory.entity.InventoryMovement;
import com.siva.shopsphere.inventory.entity.InventoryMovementType;
import com.siva.shopsphere.inventory.repository.InventoryMovementRepository;
import com.siva.shopsphere.inventory.repository.InventoryRepository;
import com.siva.shopsphere.products.entity.Brand;
import com.siva.shopsphere.products.entity.Category;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryMovementRepository movementRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("MacBook Air M3");
        product.setSku("MBA-M3-256");
        product.setPrice(new BigDecimal("99999.00"));
        product.setActive(true);
        product.setCategory(activeCategory());
        product.setBrand(activeBrand());

        inventory = new Inventory();
        inventory.setId(UUID.randomUUID());
        inventory.setProduct(product);
        inventory.setQuantity(100);
        inventory.setReservedQuantity(20);
        inventory.setLowStockThreshold(10);
    }

    @Test
    void createInventoryCreatesInitialMovement() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.existsByProductId(product.getId())).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = inventoryService.createInventory(new CreateInventoryRequest(product.getId(), 100, 10));

        assertThat(response.quantity()).isEqualTo(100);
        assertThat(response.availableQuantity()).isEqualTo(100);
        verify(movementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void duplicateInventoryIsRejected() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.existsByProductId(product.getId())).thenReturn(true);

        assertThatThrownBy(() -> inventoryService.createInventory(new CreateInventoryRequest(product.getId(), 100, 10)))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void stockInIncreasesQuantity() {
        when(inventoryRepository.findByProductIdWithLock(product.getId())).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = inventoryService.adjustStock(product.getId(), new StockAdjustmentRequest(
            StockAdjustmentRequest.Type.STOCK_IN,
            50,
            "Supplier shipment"
        ));

        assertThat(response.quantity()).isEqualTo(150);
    }

    @Test
    void stockOutCannotExceedAvailableQuantity() {
        when(inventoryRepository.findByProductIdWithLock(product.getId())).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.adjustStock(product.getId(), new StockAdjustmentRequest(
            StockAdjustmentRequest.Type.STOCK_OUT,
            90,
            "Too many"
        ))).isInstanceOf(BadRequestException.class);
    }

    @Test
    void adjustmentSupportsPositiveAndNegativeChanges() {
        when(inventoryRepository.findByProductIdWithLock(product.getId())).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = inventoryService.adjustStock(product.getId(), new StockAdjustmentRequest(
            StockAdjustmentRequest.Type.ADJUSTMENT,
            -5,
            "Correction"
        ));

        assertThat(response.quantity()).isEqualTo(95);
    }

    @Test
    void movementHistoryReturnsNewestFirst() {
        InventoryMovement first = movement("first", InventoryMovementType.STOCK_IN);
        InventoryMovement second = movement("second", InventoryMovementType.STOCK_OUT);
        when(inventoryRepository.findByProductId(product.getId())).thenReturn(Optional.of(inventory));
        when(movementRepository.findByInventoryIdOrderByCreatedAtDesc(inventory.getId())).thenReturn(List.of(second, first));

        var history = inventoryService.getMovementHistory(product.getId());

        assertThat(history).hasSize(2);
        assertThat(history.get(0).reason()).isEqualTo("second");
    }

    @Test
    void lowStockCalculationWorks() {
        inventory.setQuantity(15);
        inventory.setReservedQuantity(0);
        when(inventoryRepository.findLowStockInventory()).thenReturn(List.of());

        assertThat(inventoryService.getLowStockInventory()).isEmpty();
    }

    @Test
    void missingProductIsRejected() {
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.createInventory(new CreateInventoryRequest(product.getId(), 100, 10)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    private Category activeCategory() {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setActive(true);
        return category;
    }

    private Brand activeBrand() {
        Brand brand = new Brand();
        brand.setId(UUID.randomUUID());
        brand.setName("Apple");
        brand.setSlug("apple");
        brand.setActive(true);
        return brand;
    }

    private InventoryMovement movement(String reason, InventoryMovementType type) {
        InventoryMovement movement = new InventoryMovement();
        movement.setId(UUID.randomUUID());
        movement.setInventory(inventory);
        movement.setReason(reason);
        movement.setType(type);
        movement.setQuantity(1);
        return movement;
    }
}
