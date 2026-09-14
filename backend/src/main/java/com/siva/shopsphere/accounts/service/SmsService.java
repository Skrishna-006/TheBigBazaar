package com.siva.shopsphere.accounts.service;

public interface SmsService {
    void sendSms(String phoneNumber, String message);
}
