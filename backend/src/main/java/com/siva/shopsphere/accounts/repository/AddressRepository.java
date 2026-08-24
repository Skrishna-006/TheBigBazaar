package com.siva.shopsphere.accounts.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.siva.shopsphere.accounts.entity.Address;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findAllByUserId(UUID userId);
    Optional<Address> findByIdAndUserId(UUID id, UUID userId);
    void deleteByIdAndUserId(UUID id, UUID userId);
    Optional<Address> findFirstByUserIdOrderByCreatedAtAsc(UUID userId);
    List<Address> findAllByUserIdOrderByCreatedAtAsc(UUID userId);
}
