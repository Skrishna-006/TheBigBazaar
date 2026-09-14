@echo off
curl -i -X POST http://localhost:8081/api/v1/auth/password-reset/send-otp -H "Content-Type: application/json" -d "{\"email\":\"existing-user-test@example.com\"}"
