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

    @Value("${app.email.log-otp:false}")
    private boolean logOtp;

    public EmailOtpProvider(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void deliverOtp(String destination, String rawOtp) {
        String subject = "Your TheBigBazaar verification code";
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setReplyTo(fromEmail);
            message.setTo(destination);
            message.setSubject(subject);
            message.setText("Hello,\n\n" +
                    "Your TheBigBazaar verification code is:\n\n" +
                    rawOtp + "\n\n" +
                    "This code expires in 5 minutes.\n\n" +
                    "If you did not request this verification code, you can safely ignore this email.\n\n" +
                    "For your security, do not share this code with anyone.\n\n" +
                    "Regards,\n" +
                    "TheBigBazaar Team");

            logger.info("==================================================");
            logger.info("TheBigBazaar OTP Email");
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

            mailSender.send(message);

            logger.info("==================================================");
            logger.info("TheBigBazaar OTP Email");
            logger.info("==================================================");
            logger.info("To: {}", destination);
            logger.info("Status: SENT TO SMTP SERVER");
            logger.info("==================================================");
            
        } catch (MailException e) {
            logger.error("==================================================");
            logger.error("TheBigBazaar OTP Email");
            logger.error("==================================================");
            logger.error("To: {}", destination);
            logger.error("Status: FAILED");
            logger.error("Error: {}", e.getMessage());
            logger.error("==================================================");
            
            throw new ServiceUnavailableException("Unable to send OTP via email at this time. Please try again later.");
        }
    }
}
