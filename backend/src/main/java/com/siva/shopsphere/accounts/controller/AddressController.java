package com.siva.shopsphere.accounts.controller;

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

import com.siva.shopsphere.accounts.dto.AddressResponse;
import com.siva.shopsphere.accounts.dto.CreateAddressRequest;
import com.siva.shopsphere.accounts.dto.UpdateAddressRequest;
import com.siva.shopsphere.accounts.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/addresses")
@SecurityRequirement(name = "bearer-jwt")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @Operation(summary = "Create a new address for the authenticated user")
    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(request));
    }

    @Operation(summary = "List addresses for the authenticated user")
    @GetMapping
    public ResponseEntity<List<AddressResponse>> list() {
        return ResponseEntity.ok(addressService.getCurrentUserAddresses());
    }

    @Operation(summary = "Get one address owned by the authenticated user")
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(addressService.getAddress(id));
    }

    @Operation(summary = "Update one address owned by the authenticated user")
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(id, request));
    }

    @Operation(summary = "Delete one address owned by the authenticated user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Set one address as the authenticated user's default address")
    @PostMapping("/{id}/default")
    public ResponseEntity<Void> setDefault(@PathVariable UUID id) {
        addressService.setDefaultAddress(id);
        return ResponseEntity.noContent().build();
    }
}
