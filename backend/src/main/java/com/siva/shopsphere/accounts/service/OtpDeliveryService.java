package com.siva.shopsphere.accounts.service;

public interface OtpDeliveryService {
    void deliverOtp(String destination, String rawOtp);
}
