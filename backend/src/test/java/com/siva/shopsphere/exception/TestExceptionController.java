package com.siva.shopsphere.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/test")
class TestExceptionController {

    @PostMapping("/validation")
    ResponseEntity<Void> validation(@Valid @RequestBody ValidationRequest request) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/not-found")
    void notFound() {
        throw new ResourceNotFoundException("Test resource not found");
    }

    @GetMapping("/bad-request")
    void badRequest() {
        throw new BadRequestException("Test bad request");
    }

    @GetMapping("/unexpected")
    void unexpected() {
        throw new IllegalStateException("Unexpected test failure");
    }

    @GetMapping("/validated/{email}")
    ResponseEntity<Void> validatePath(@Email @PathVariable String email) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    static class ValidationRequest {
        @NotBlank(message = "Password is required")
        private String password;

        @Email(message = "Invalid email")
        @NotBlank(message = "Email is required")
        private String email;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}

