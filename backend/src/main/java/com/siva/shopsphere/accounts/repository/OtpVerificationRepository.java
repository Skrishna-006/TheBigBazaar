package com.siva.shopsphere.accounts.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.siva.shopsphere.accounts.entity.OtpPurpose;
import com.siva.shopsphere.accounts.entity.OtpVerification;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {
    Optional<OtpVerification> findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc(String phoneNumber, OtpPurpose purpose);
}
