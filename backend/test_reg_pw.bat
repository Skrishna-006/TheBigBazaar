@echo off
curl -i -X POST http://localhost:8081/api/v1/auth/register/send-otp -H "Content-Type: application/json" -d "{\"email\":\"test-pw@example.com\"}"
