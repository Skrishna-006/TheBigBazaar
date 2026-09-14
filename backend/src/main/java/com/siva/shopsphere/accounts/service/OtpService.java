package com.siva.shopsphere.accounts.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siva.shopsphere.accounts.entity.OtpPurpose;
import com.siva.shopsphere.accounts.entity.OtpVerification;
import com.siva.shopsphere.accounts.repository.OtpVerificationRepository;
import com.siva.shopsphere.exception.BadRequestException;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final int MAX_ATTEMPTS = 5;

    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;
    private final OtpDeliveryService emailDeliveryService;
    private final SecureRandom secureRandom;

    public OtpService(OtpVerificationRepository otpRepository, PasswordEncoder passwordEncoder, SmsService smsService, OtpDeliveryService emailDeliveryService) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.smsService = smsService;
        this.emailDeliveryService = emailDeliveryService;
        this.secureRandom = new SecureRandom();
    }

    @Transactional
    public void sendOtp(String phoneNumber, OtpPurpose purpose) {
        // Invalidate previous active OTP if exists
        Optional<OtpVerification> existing = otpRepository.findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc(phoneNumber, purpose);
        if (existing.isPresent()) {
            OtpVerification otp = existing.get();
            if (otp.getConsumedAt() == null && otp.getExpiresAt().isAfter(Instant.now())) {
                // Rate limit resend to 1 minute
                if (otp.getCreatedAt().plus(1, ChronoUnit.MINUTES).isAfter(Instant.now())) {
                    throw new BadRequestException("Please wait before requesting a new OTP");
                }
                otp.setConsumedAt(Instant.now()); // Invalidate
                otpRepository.save(otp);
            }
        }

        String rawOtp = generateNumericOtp(OTP_LENGTH);
        
        OtpVerification verification = new OtpVerification();
        verification.setPhoneNumber(phoneNumber);
        verification.setOtpHash(passwordEncoder.encode(rawOtp));
        verification.setPurpose(purpose);
        verification.setExpiresAt(Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        
        otpRepository.save(verification);
        
        if (phoneNumber.contains("@")) {
            emailDeliveryService.deliverOtp(phoneNumber, rawOtp);
        } else {
            String message = String.format("Your TheBigBazaar verification code is: %s", rawOtp);
            smsService.sendSms(phoneNumber, message);
        }
    }

    @Transactional
    public String verifyOtp(String phoneNumber, String otp, OtpPurpose purpose) {
        OtpVerification verification = otpRepository.findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc(phoneNumber, purpose)
            .orElseThrow(() -> new BadRequestException("No active OTP found. Please request a new one."));
            
        if (verification.getConsumedAt() != null) {
            throw new BadRequestException("This OTP has already been used. Please request a new one.");
        }
        
        if (verification.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("This OTP has expired. Please request a new one.");
        }
        
        if (verification.getAttemptCount() >= MAX_ATTEMPTS) {
            verification.setConsumedAt(Instant.now());
            otpRepository.save(verification);
            throw new BadRequestException("Maximum attempts reached. Please request a new OTP.");
        }
        
        if (!passwordEncoder.matches(otp, verification.getOtpHash())) {
            verification.setAttemptCount(verification.getAttemptCount() + 1);
            otpRepository.save(verification);
            throw new BadRequestException("Invalid OTP.");
        }
        
        // OTP is valid
        verification.setConsumedAt(Instant.now());
        
        // Generate a secure token to proceed to the next step
        String rawToken = UUID.randomUUID().toString();
        verification.setResetTokenHash(passwordEncoder.encode(rawToken));
        verification.setResetTokenExpiresAt(Instant.now().plus(TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        
        otpRepository.save(verification);
        
        return rawToken;
    }
    
    @Transactional
    public OtpVerification validateToken(String phoneNumber, String token, OtpPurpose purpose) {
        OtpVerification verification = otpRepository.findTopByPhoneNumberAndPurposeOrderByCreatedAtDesc(phoneNumber, purpose)
            .orElseThrow(() -> new BadRequestException("Invalid session."));
            
        if (verification.getResetTokenHash() == null || verification.getResetTokenExpiresAt() == null) {
            throw new BadRequestException("Invalid session.");
        }
        
        if (verification.getResetTokenExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Session expired. Please start over.");
        }
        
        if (!passwordEncoder.matches(token, verification.getResetTokenHash())) {
            throw new BadRequestException("Invalid token.");
        }
        
        // Invalidate token so it can't be reused
        verification.setResetTokenExpiresAt(Instant.now());
        otpRepository.save(verification);
        
        return verification;
    }

    private String generateNumericOtp(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}
