package com.siva.shopsphere.accounts.mapper;

import com.siva.shopsphere.accounts.dto.AddressResponse;
import com.siva.shopsphere.accounts.dto.CreateAddressRequest;
import com.siva.shopsphere.accounts.dto.UpdateAddressRequest;
import com.siva.shopsphere.accounts.entity.Address;

public final class AddressMapper {

    private AddressMapper() {
    }

    public static Address toEntity(CreateAddressRequest request) {
        Address address = new Address();
        apply(address, request.firstName(), request.lastName(), request.phone(), request.addressLine1(), request.addressLine2(), request.city(), request.state(), request.postalCode(), request.country(), request.defaultAddress());
        return address;
    }

    public static void updateEntity(Address address, UpdateAddressRequest request) {
        apply(address, request.firstName(), request.lastName(), request.phone(), request.addressLine1(), request.addressLine2(), request.city(), request.state(), request.postalCode(), request.country(), request.defaultAddress());
    }

    public static AddressResponse toResponse(Address address) {
        return new AddressResponse(
            address.getId(),
            address.getFirstName(),
            address.getLastName(),
            address.getPhone(),
            address.getAddressLine1(),
            address.getAddressLine2(),
            address.getCity(),
            address.getState(),
            address.getPostalCode(),
            address.getCountry(),
            address.isDefaultAddress(),
            address.getCreatedAt(),
            address.getUpdatedAt()
        );
    }

    private static void apply(Address address, String firstName, String lastName, String phone, String addressLine1, String addressLine2, String city, String state, String postalCode, String country, Boolean defaultAddress) {
        if (firstName != null) {
            address.setFirstName(firstName.trim());
        }
        if (lastName != null) {
            address.setLastName(lastName.trim());
        }
        if (phone != null) {
            address.setPhone(phone.trim());
        }
        if (addressLine1 != null) {
            address.setAddressLine1(addressLine1.trim());
        }
        if (addressLine2 != null) {
            address.setAddressLine2(addressLine2.trim());
        }
        if (city != null) {
            address.setCity(city.trim());
        }
        if (state != null) {
            address.setState(state.trim());
        }
        if (postalCode != null) {
            address.setPostalCode(postalCode.trim());
        }
        if (country != null) {
            address.setCountry(country.trim());
        }
        if (defaultAddress != null) {
            address.setDefaultAddress(defaultAddress);
        }
    }
}
