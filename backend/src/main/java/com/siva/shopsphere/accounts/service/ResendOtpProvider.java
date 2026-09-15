package com.siva.shopsphere.accounts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.siva.shopsphere.exception.ServiceUnavailableException;

import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "otp.delivery", havingValue = "resend")
public class ResendOtpProvider implements OtpDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(ResendOtpProvider.class);

    private final String apiKey;
    private final String fromEmail;
    private final boolean logOtp;
    
    private Resend resend;

    public ResendOtpProvider(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-email}") String fromEmail,
            @Value("${app.email.log-otp:false}") boolean logOtp) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.logOtp = logOtp;
    }

    @PostConstruct
    public void init() {
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Resend API key is not configured. Please set RESEND_API_KEY environment variable.");
        }
        if (this.fromEmail == null || this.fromEmail.trim().isEmpty()) {
            throw new IllegalStateException("Resend from-email is not configured. Please set RESEND_FROM_EMAIL environment variable.");
        }
        this.resend = new Resend(this.apiKey);
    }

    @Override
    public void deliverOtp(String destination, String rawOtp) {
        String subject = "Your TheBigBazaar verification code";
        String bodyText = "Hello,\n\n" +
                "Your TheBigBazaar verification code is:\n\n" +
                rawOtp + "\n\n" +
                "This code expires in 5 minutes.\n\n" +
                "If you did not request this verification code, you can safely ignore this email.\n\n" +
                "For your security, do not share this code with anyone.\n\n" +
                "Regards,\n" +
                "TheBigBazaar Team";

        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(destination)
                    .subject(subject)
                    .text(bodyText) // Resend allows setting both html and text, or just text
                    .build();

            logger.info("==================================================");
            logger.info("TheBigBazaar OTP Email (Resend)");
            logger.info("==================================================");
            logger.info("To: {}", destination);
            logger.info("From: {}", fromEmail);
            logger.info("Subject: {}", subject);
            if (logOtp) {
                logger.info("OTP: {}", rawOtp);
            } else {
                logger.info("OTP: [HIDDEN]");
            }
            logger.info("Expires: 5 minutes");
            logger.info("Status: SENDING");
            logger.info("==================================================");

            resend.emails().send(params);

            logger.info("==================================================");
            logger.info("TheBigBazaar OTP Email (Resend)");
            logger.info("==================================================");
            logger.info("To: {}", destination);
            logger.info("Status: SENT VIA RESEND API");
            logger.info("==================================================");

        } catch (ResendException e) {
            logger.error("==================================================");
            logger.error("TheBigBazaar OTP Email (Resend)");
            logger.error("==================================================");
            logger.error("To: {}", destination);
            logger.error("Status: FAILED");
            // Do NOT log the full exception if it might leak API keys or sensitive data, 
            // but getting the message is usually safe. 
            logger.error("Error: {}", e.getMessage());
            logger.error("==================================================");

            throw new ServiceUnavailableException("Unable to send OTP via email at this time. Please try again later.");
        }
    }
}
