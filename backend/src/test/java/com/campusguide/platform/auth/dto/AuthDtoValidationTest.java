package com.campusguide.platform.auth.dto;

import com.campusguide.platform.auth.dto.request.LoginRequest;
import com.campusguide.platform.auth.dto.request.RefreshTokenRequest;
import com.campusguide.platform.auth.dto.request.RegisterRequest;
import com.campusguide.platform.auth.dto.response.AuthenticationResponse;
import com.campusguide.platform.auth.dto.response.ValidationErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registerRequest_ValidData_NoViolations() {
        RegisterRequest request = RegisterRequest.builder()
                .email("user@example.com")
                .username("valid_user")
                .password("SecureP@ss123")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void registerRequest_InvalidEmailAndWeakPassword_HasViolations() {
        RegisterRequest request = RegisterRequest.builder()
                .email("invalid-email")
                .username("ab") // too short
                .password("weak")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertEquals(3, violations.size());
    }

    @Test
    void registerRequest_BlankFields_HasViolations() {
        RegisterRequest request = RegisterRequest.builder()
                .email("")
                .username("")
                .password("")
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void loginRequest_ValidData_NoViolations() {
        LoginRequest request = LoginRequest.builder()
                .emailOrUsername("user@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void loginRequest_BlankFields_HasViolations() {
        LoginRequest request = LoginRequest.builder()
                .emailOrUsername("")
                .password("")
                .build();

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }

    @Test
    void refreshTokenRequest_ValidData_NoViolations() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void refreshTokenRequest_BlankToken_HasViolations() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("")
                .build();

        Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void authenticationResponse_Structure() {
        java.time.Instant now = java.time.Instant.now();
        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600L)
                .issuedAt(now)
                .build();

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals(now, response.getIssuedAt());
    }

    @Test
    void validationErrorResponse_Structure() {
        LocalDateTime now = LocalDateTime.now();
        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .timestamp(now)
                .status(400)
                .message("Validation failed")
                .errors(Map.of("email", "Email is required"))
                .build();

        assertEquals(now, response.getTimestamp());
        assertEquals(400, response.getStatus());
        assertEquals("Validation failed", response.getMessage());
        assertEquals("Email is required", response.getErrors().get("email"));
    }
}
