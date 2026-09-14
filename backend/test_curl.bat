@echo off
curl -i -X POST http://localhost:8080/api/v1/auth/register/send-otp -H "Content-Type: application/json" -d "{\"email\":\"test-curl-2@example.com\"}"
