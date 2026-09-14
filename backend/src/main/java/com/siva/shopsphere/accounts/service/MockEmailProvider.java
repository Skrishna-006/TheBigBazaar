package com.siva.shopsphere.accounts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "otp.delivery", havingValue = "mock", matchIfMissing = true)
public class MockEmailProvider implements OtpDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(MockEmailProvider.class);

    @Override
    public void deliverOtp(String destination, String rawOtp) {
        // DO NOT log this in a real production environment!
        // This is solely for development/testing as requested.
        logger.info("==========================================");
        logger.info("MOCK EMAIL SENT TO: {}", destination);
        logger.info("SUBJECT: TheBigBazaar Verification Code");
        logger.info("BODY: Hello,\n\nYour TheBigBazaar verification code is:\n\n{}\n\nThis code will expire in 5 minutes.\n\nIf you did not request this code, you can safely ignore this email.\n\nDo not share this code with anyone.\n\nRegards,\nTheBigBazaar Team", rawOtp);
        logger.info("==========================================");
    }
}
