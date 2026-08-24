package com.siva.shopsphere.accounts.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.siva.shopsphere.accounts.dto.AddressResponse;
import com.siva.shopsphere.accounts.dto.CreateAddressRequest;
import com.siva.shopsphere.accounts.dto.UpdateAddressRequest;
import com.siva.shopsphere.accounts.entity.Address;
import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.entity.UserRole;
import com.siva.shopsphere.accounts.repository.AddressRepository;
import com.siva.shopsphere.exception.ResourceNotFoundException;
import com.siva.shopsphere.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AddressService addressService;

    private User user;
    private Address addressA;
    private Address addressB;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("customer@example.com");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        user.setEmailVerified(false);

        addressA = address("A", true, Instant.parse("2026-08-24T00:00:00Z"));
        addressB = address("B", false, Instant.parse("2026-08-24T00:05:00Z"));
        addressA.setUser(user);
        addressB.setUser(user);

        when(currentUserService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void createAddressAssociatesAuthenticatedUser() {
        when(addressRepository.findAllByUserId(user.getId())).thenReturn(List.of());
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address address = invocation.getArgument(0);
            address.setId(UUID.randomUUID());
            return address;
        });

        AddressResponse response = addressService.createAddress(new CreateAddressRequest(
            "Siva", "Krishna", "9876543210", "Main Road", "Near College", "Anantapur", "Andhra Pradesh", "515001", "India", false
        ));

        assertThat(response.defaultAddress()).isTrue();
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void listReturnsOnlyCurrentUsersAddresses() {
        when(addressRepository.findAllByUserIdOrderByCreatedAtAsc(user.getId())).thenReturn(List.of(addressA, addressB));

        List<AddressResponse> responses = addressService.getCurrentUserAddresses();

        assertThat(responses).hasSize(2);
    }

    @Test
    void getOwnAddressReturnsAddress() {
        when(addressRepository.findByIdAndUserId(addressA.getId(), user.getId())).thenReturn(Optional.of(addressA));

        AddressResponse response = addressService.getAddress(addressA.getId());

        assertThat(response.id()).isEqualTo(addressA.getId());
    }

    @Test
    void getAnotherUsersAddressReturnsNotFound() {
        when(addressRepository.findByIdAndUserId(addressA.getId(), user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddress(addressA.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateOwnAddressUpdatesAllowedFields() {
        when(addressRepository.findByIdAndUserId(addressA.getId(), user.getId())).thenReturn(Optional.of(addressA));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddressResponse response = addressService.updateAddress(addressA.getId(), new UpdateAddressRequest(
            "New", "Name", "9999999999", "Updated Line", null, "Updated City", "Updated State", "515002", "India", false
        ));

        assertThat(response.firstName()).isEqualTo("New");
        assertThat(addressA.getFirstName()).isEqualTo("New");
        assertThat(addressA.getLastName()).isEqualTo("Name");
        assertThat(addressA.getPhone()).isEqualTo("9999999999");
    }

    @Test
    void deleteOwnAddressDeletesAddress() {
        when(addressRepository.findByIdAndUserId(addressA.getId(), user.getId())).thenReturn(Optional.of(addressA));

        addressService.deleteAddress(addressA.getId());

        verify(addressRepository).delete(addressA);
    }

    @Test
    void setDefaultMakesOnlyOneDefault() {
        when(addressRepository.findByIdAndUserId(addressB.getId(), user.getId())).thenReturn(Optional.of(addressB));
        when(addressRepository.findAllByUserId(user.getId())).thenReturn(List.of(addressA, addressB));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        addressService.setDefaultAddress(addressB.getId());

        assertThat(addressA.isDefaultAddress()).isFalse();
        assertThat(addressB.isDefaultAddress()).isTrue();
    }

    @Test
    void deletingDefaultPromotesAnotherAddress() {
        when(addressRepository.findByIdAndUserId(addressA.getId(), user.getId())).thenReturn(Optional.of(addressA));
        when(addressRepository.findAllByUserIdOrderByCreatedAtAsc(user.getId())).thenReturn(List.of(addressB));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        addressService.deleteAddress(addressA.getId());

        assertThat(addressB.isDefaultAddress()).isTrue();
    }

    private Address address(String suffix, boolean defaultAddress, Instant createdAt) {
        Address address = new Address();
        address.setId(UUID.nameUUIDFromBytes(("address-" + suffix).getBytes()));
        address.setFirstName("Siva");
        address.setLastName("Krishna");
        address.setPhone("9876543210");
        address.setAddressLine1("Main Road");
        address.setAddressLine2("Near College");
        address.setCity("Anantapur");
        address.setState("Andhra Pradesh");
        address.setPostalCode("515001");
        address.setCountry("India");
        address.setDefaultAddress(defaultAddress);
        try {
            var createdField = Address.class.getDeclaredField("createdAt");
            createdField.setAccessible(true);
            createdField.set(address, createdAt);
            var updatedField = Address.class.getDeclaredField("updatedAt");
            updatedField.setAccessible(true);
            updatedField.set(address, createdAt);
        } catch (Exception ignored) {
        }
        return address;
    }
}
