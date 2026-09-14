@echo off
curl -i -X POST http://localhost:8080/api/v1/auth/register/send-otp -H "Content-Type: application/json" -d "{\"email\":\"new-user-test@example.com\"}"
