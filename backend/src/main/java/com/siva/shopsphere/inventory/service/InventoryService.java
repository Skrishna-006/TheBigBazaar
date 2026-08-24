package com.siva.shopsphere.inventory.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.exception.BadRequestException;
import com.siva.shopsphere.exception.ConflictException;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.inventory.dto.CreateInventoryRequest;
import com.siva.shopsphere.inventory.dto.InventoryMovementResponse;
import com.siva.shopsphere.inventory.dto.InventoryResponse;
import com.siva.shopsphere.inventory.dto.StockAdjustmentRequest;
import com.siva.shopsphere.inventory.entity.Inventory;
import com.siva.shopsphere.inventory.entity.InventoryMovement;
import com.siva.shopsphere.inventory.entity.InventoryMovementType;
import com.siva.shopsphere.inventory.mapper.InventoryMapper;
import com.siva.shopsphere.inventory.repository.InventoryMovementRepository;
import com.siva.shopsphere.inventory.repository.InventoryRepository;
import com.siva.shopsphere.products.entity.Product;
import com.siva.shopsphere.products.repository.ProductRepository;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;

    public InventoryService(InventoryRepository inventoryRepository, InventoryMovementRepository movementRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        Product product = loadProduct(request.productId());
        if (inventoryRepository.existsByProductId(product.getId())) {
            throw new ConflictException("Inventory already exists for this product");
        }
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(request.quantity());
        inventory.setReservedQuantity(0);
        inventory.setLowStockThreshold(request.lowStockThreshold());
        Inventory saved = inventoryRepository.save(inventory);
        createMovement(saved, InventoryMovementType.STOCK_IN, request.quantity(), "Initial inventory");
        return InventoryMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        return InventoryMapper.toResponse(inventory);
    }

    @Transactional(readOnly = true)
    public int getAvailableQuantity(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        return availableQuantity(inventory);
    }

    @Transactional
    public InventoryResponse adjustStock(UUID productId, StockAdjustmentRequest request) {
        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));

        if (request.quantity() == 0) {
            throw new BadRequestException("Quantity must not be zero");
        }

        int delta = switch (request.type()) {
            case STOCK_IN -> positiveQuantity(request.quantity(), "STOCK_IN");
            case STOCK_OUT -> -positiveQuantity(request.quantity(), "STOCK_OUT");
            case ADJUSTMENT -> request.quantity();
        };

        if (request.type() == StockAdjustmentRequest.Type.STOCK_OUT) {
            if (request.quantity() > availableQuantity(inventory)) {
                throw new BadRequestException("Requested stock-out exceeds available quantity");
            }
        }

        int newQuantity = inventory.getQuantity() + delta;
        if (newQuantity < inventory.getReservedQuantity()) {
            throw new BadRequestException("Quantity cannot be less than reserved quantity");
        }
        if (newQuantity < 0) {
            throw new BadRequestException("Quantity cannot be negative");
        }

        inventory.setQuantity(newQuantity);
        Inventory saved = inventoryRepository.save(inventory);
        createMovement(saved, toMovementType(request.type()), Math.abs(request.quantity()), request.reason());
        return InventoryMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> getMovementHistory(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found"));
        return movementRepository.findByInventoryIdOrderByCreatedAtDesc(inventory.getId()).stream()
            .map(InventoryMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockInventory() {
        return inventoryRepository.findLowStockInventory().stream()
            .map(InventoryMapper::toResponse)
            .toList();
    }

    private Product loadProduct(UUID productId) {
        return productRepository.findById(productId)
            .filter(Product::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private InventoryMovement createMovement(Inventory inventory, InventoryMovementType type, int quantity, String reason) {
        InventoryMovement movement = new InventoryMovement();
        movement.setInventory(inventory);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setReason(reason);
        return movementRepository.save(movement);
    }

    private int positiveQuantity(Integer quantity, String type) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException(type + " quantity must be greater than zero");
        }
        return quantity;
    }

    private int availableQuantity(Inventory inventory) {
        return inventory.getQuantity() - inventory.getReservedQuantity();
    }

    private InventoryMovementType toMovementType(StockAdjustmentRequest.Type type) {
        return switch (type) {
            case STOCK_IN -> InventoryMovementType.STOCK_IN;
            case STOCK_OUT -> InventoryMovementType.STOCK_OUT;
            case ADJUSTMENT -> InventoryMovementType.ADJUSTMENT;
        };
    }
}
