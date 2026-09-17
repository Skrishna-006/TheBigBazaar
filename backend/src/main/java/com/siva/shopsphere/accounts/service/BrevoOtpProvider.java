package com.siva.shopsphere.accounts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.siva.shopsphere.exception.ServiceUnavailableException;

import jakarta.annotation.PostConstruct;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "otp.delivery", havingValue = "brevo")
public class BrevoOtpProvider implements OtpDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(BrevoOtpProvider.class);

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final String apiKey;
    private final String fromEmail;
    private final String fromName;
    
    private RestTemplate restTemplate;

    public BrevoOtpProvider(
            @Value("${brevo.api-key}") String apiKey,
            @Value("${brevo.from-email}") String fromEmail,
            @Value("${brevo.from-name}") String fromName) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    @PostConstruct
    public void init() {
        if (this.apiKey == null || this.apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Brevo API key is not configured. Please set BREVO_API_KEY environment variable.");
        }
        if (this.fromEmail == null || this.fromEmail.trim().isEmpty()) {
            throw new IllegalStateException("Brevo from-email is not configured. Please set BREVO_FROM_EMAIL environment variable.");
        }
        this.restTemplate = new RestTemplate();
    }

    // visible for testing
    void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("api-key", this.apiKey);

            Map<String, Object> sender = new HashMap<>();
            sender.put("email", this.fromEmail);
            if (this.fromName != null && !this.fromName.trim().isEmpty()) {
                sender.put("name", this.fromName);
            }

            Map<String, Object> to = new HashMap<>();
            to.put("email", destination);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sender", sender);
            requestBody.put("to", Collections.singletonList(to));
            requestBody.put("subject", subject);
            requestBody.put("textContent", bodyText);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            logger.info("==================================================");
            logger.info("TheBigBazaar OTP Email (Brevo)");
            logger.info("==================================================");
            logger.info("To: {}", destination);
            logger.info("From: {} ({})", fromEmail, fromName != null && !fromName.isEmpty() ? fromName : "N/A");
            logger.info("Subject: {}", subject);
            logger.info("OTP: [HIDDEN]");
            logger.info("Expires: 5 minutes");
            logger.info("Status: SENDING");
            logger.info("==================================================");

            ResponseEntity<String> response = restTemplate.exchange(
                    BREVO_API_URL, HttpMethod.POST, requestEntity, String.class);

            logger.info("==================================================");
            logger.info("TheBigBazaar OTP Email (Brevo)");
            logger.info("==================================================");
            logger.info("To: {}", destination);
            logger.info("Status: SENT VIA BREVO API");
            logger.info("==================================================");

        } catch (RestClientException e) {
            logger.error("==================================================");
            logger.error("TheBigBazaar OTP Email (Brevo)");
            logger.error("==================================================");
            logger.error("To: {}", destination);
            logger.error("Status: FAILED");
            // getting the message is safe, avoid logging full stack trace which might leak info
            logger.error("Error: {}", e.getMessage());
            logger.error("==================================================");

            throw new ServiceUnavailableException("Unable to send OTP via email at this time. Please try again later.");
        }
    }
}
