package com.siva.shopsphere.accounts.service;

public interface PasswordResetEmailService {
    void sendPasswordResetEmail(String toEmail, String resetLink);
}
