package com.campusguide.platform.user.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid User with all required fields")
    void testValidUserCreation() {
        Instant now = Instant.now();
        User user = User.builder()
                .id("user-123")
                .email("student@campusguide.com")
                .username("student123")
                .passwordHash("hashed_password_123")
                .role(UserRole.STUDENT)
                .enabled(true)
                .emailVerified(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid user should produce 0 constraint violations");

        assertEquals("user-123", user.getId());
        assertEquals("student@campusguide.com", user.getEmail());
        assertEquals("student123", user.getUsername());
        assertEquals("hashed_password_123", user.getPasswordHash());
        assertEquals(UserRole.STUDENT, user.getRole());
        assertTrue(user.isEnabled());
        assertTrue(user.isEmailVerified());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    @DisplayName("Should fail validation on invalid email")
    void testInvalidEmail() {
        User user = User.builder()
                .email("not-an-email")
                .username("validuser")
                .passwordHash("hashed_pass")
                .role(UserRole.STUDENT)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Should fail validation on blank username or short username")
    void testInvalidUsername() {
        User userShort = User.builder()
                .email("user@campusguide.com")
                .username("ab")
                .passwordHash("hashed_pass")
                .role(UserRole.STUDENT)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(userShort);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    @DisplayName("Should fail validation when role is null")
    void testNullRole() {
        User user = User.builder()
                .email("user@campusguide.com")
                .username("validuser")
                .passwordHash("hashed_pass")
                .role(null)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }

    @Test
    @DisplayName("Should verify default boolean flags")
    void testDefaultValues() {
        User user = User.builder()
                .email("default@campusguide.com")
                .username("defaultuser")
                .passwordHash("hash")
                .role(UserRole.ADMIN)
                .build();

        assertTrue(user.isEnabled());
        assertFalse(user.isEmailVerified());
    }
}
