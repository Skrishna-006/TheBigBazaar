package com.siva.shopsphere.accounts.service;

import com.siva.shopsphere.exception.BadRequestException;

public class PhoneNumberUtils {
    
    public static String normalize(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new BadRequestException("Phone number is required");
        }
        // Remove all non-numeric characters except leading +
        String normalized = phoneNumber.trim().replaceAll("[^\\d+]", "");
        
        if (normalized.length() < 10 || normalized.length() > 15) {
            throw new BadRequestException("Invalid phone number format");
        }
        
        return normalized;
    }
}
