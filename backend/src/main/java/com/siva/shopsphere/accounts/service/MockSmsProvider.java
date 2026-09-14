package com.siva.shopsphere.accounts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
public class MockSmsProvider implements SmsService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockSmsProvider.class);

    @Override
    public void sendSms(String phoneNumber, String message) {
        // DO NOT log this in a real production environment!
        // This is solely for development/testing as requested.
        logger.info("==========================================");
        logger.info("MOCK SMS SENT TO: {}", phoneNumber);
        logger.info("MESSAGE: {}", message);
        logger.info("==========================================");
    }
}
