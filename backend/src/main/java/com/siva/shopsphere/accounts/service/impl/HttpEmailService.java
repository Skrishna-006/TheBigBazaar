package com.siva.shopsphere.accounts.service.impl;

import com.siva.shopsphere.accounts.service.PasswordResetEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class HttpEmailService implements PasswordResetEmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpEmailService.class);

    @Value("${app.email.provider.api-url:https://api.resend.com/emails}")
    private String apiUrl;

    @Value("${app.email.provider.api-key:}")
    private String apiKey;

    @Value("${app.email.provider.sender-email:noreply@thebigbazaar.com}")
    private String senderEmail;

    private final HttpClient httpClient;

    public HttpEmailService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("Email API key is not configured. Cannot send password reset email to {}.", toEmail);
            throw new IllegalStateException("Email API key is not configured");
        }

        try {
            String htmlContent = String.format(
                "<h2>TheBigBazaar - Password Reset</h2>" +
                "<p>You requested a password reset. Click the link below to reset your password. This link will expire in 15 minutes.</p>" +
                "<p><a href=\"%s\">Reset Password</a></p>" +
                "<p>If you did not request this, please ignore this email and your password will remain unchanged.</p>",
                resetLink
            );

            String jsonPayload = String.format(
                "{\"from\": \"%s\", \"to\": [\"%s\"], \"subject\": \"TheBigBazaar - Password Reset\", \"html\": \"%s\"}",
                senderEmail, toEmail, htmlContent.replace("\"", "\\\"")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                logger.info("Password reset email sent to {}", toEmail);
            } else {
                logger.error("Failed to send email. Status: {}", response.statusCode());
                throw new RuntimeException("Email provider rejected the request");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while sending password reset email");
            throw new RuntimeException("Email sending was interrupted", e);
        } catch (Exception e) {
            logger.error("Exception occurred while sending password reset email: {}", e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
