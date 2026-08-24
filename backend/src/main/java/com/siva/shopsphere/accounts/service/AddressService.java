package com.siva.shopsphere.accounts.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.accounts.dto.AddressResponse;
import com.siva.shopsphere.accounts.dto.CreateAddressRequest;
import com.siva.shopsphere.accounts.dto.UpdateAddressRequest;
import com.siva.shopsphere.accounts.entity.Address;
import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.mapper.AddressMapper;
import com.siva.shopsphere.accounts.repository.AddressRepository;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.security.CurrentUserService;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CurrentUserService currentUserService;

    public AddressService(AddressRepository addressRepository, CurrentUserService currentUserService) {
        this.addressRepository = addressRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public AddressResponse createAddress(CreateAddressRequest request) {
        User user = currentUserService.getCurrentUser();
        Address address = AddressMapper.toEntity(request);
        address.setUser(user);
        boolean isFirstAddress = addressRepository.findAllByUserId(user.getId()).isEmpty();
        address.setDefaultAddress(Boolean.TRUE.equals(request.defaultAddress()) || isFirstAddress);
        if (address.isDefaultAddress()) {
            unsetOtherDefaults(user.getId());
        }
        return AddressMapper.toResponse(addressRepository.save(address));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getCurrentUserAddresses() {
        User user = currentUserService.getCurrentUser();
        return addressRepository.findAllByUserIdOrderByCreatedAtAsc(user.getId()).stream()
            .map(AddressMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddress(UUID id) {
        return AddressMapper.toResponse(getOwnedAddress(id));
    }

    @Transactional
    public AddressResponse updateAddress(UUID id, UpdateAddressRequest request) {
        Address address = getOwnedAddress(id);
        AddressMapper.updateEntity(address, request);
        if (Boolean.TRUE.equals(request.defaultAddress())) {
            setDefaultAddress(id);
            return AddressMapper.toResponse(address);
        }
        return AddressMapper.toResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(UUID id) {
        User user = currentUserService.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);
        if (wasDefault) {
            promoteReplacementDefault(user.getId());
        }
    }

    @Transactional
    public void setDefaultAddress(UUID id) {
        User user = currentUserService.getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        unsetOtherDefaults(user.getId());
        address.setDefaultAddress(true);
        addressRepository.save(address);
    }

    private Address getOwnedAddress(UUID id) {
        User user = currentUserService.getCurrentUser();
        return addressRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    private void unsetOtherDefaults(UUID userId) {
        for (Address existing : addressRepository.findAllByUserId(userId)) {
            if (existing.isDefaultAddress()) {
                existing.setDefaultAddress(false);
                addressRepository.save(existing);
            }
        }
    }

    private void promoteReplacementDefault(UUID userId) {
        List<Address> addresses = addressRepository.findAllByUserIdOrderByCreatedAtAsc(userId);
        if (addresses.isEmpty()) {
            return;
        }
        Address replacement = addresses.stream()
            .max(Comparator.comparing(Address::getCreatedAt))
            .orElse(addresses.getFirst());
        replacement.setDefaultAddress(true);
        addressRepository.save(replacement);
    }
}
