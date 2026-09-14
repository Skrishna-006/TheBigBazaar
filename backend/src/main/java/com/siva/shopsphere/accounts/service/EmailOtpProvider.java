package com.siva.shopsphere.accounts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.siva.shopsphere.exception.ServiceUnavailableException;

@Service
@ConditionalOnProperty(name = "otp.delivery", havingValue = "email")
public class EmailOtpProvider implements OtpDeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(EmailOtpProvider.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailOtpProvider(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void deliverOtp(String destination, String rawOtp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(destination);
            message.setSubject("TheBigBazaar Verification Code");
            message.setText("Hello,\n\n" +
                    "Your TheBigBazaar verification code is:\n\n" +
                    rawOtp + "\n\n" +
                    "This code will expire in 5 minutes.\n\n" +
                    "If you did not request this code, you can safely ignore this email.\n\n" +
                    "Do not share this code with anyone.\n\n" +
                    "Regards,\n" +
                    "TheBigBazaar Team");

            mailSender.send(message);
        } catch (MailException e) {
            logger.error("Failed to send email to {}", destination, e);
            throw new ServiceUnavailableException("Unable to send OTP via email at this time. Please try again later.");
        }
    }
}
