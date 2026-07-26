package com.campusguide.platform.auth.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordStrengthValidatorTest {

    private PasswordStrengthValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordStrengthValidator();
    }

    @Test
    void isValid_NullPassword_ReturnsTrue() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void isValid_ValidPassword_ReturnsTrue() {
        assertTrue(validator.isValid("P@ssword1", null));
        assertTrue(validator.isValid("Complex#Password99", null));
    }

    @Test
    void isValid_TooShort_ReturnsFalse() {
        assertFalse(validator.isValid("P@ss1", null));
    }

    @Test
    void isValid_MissingUppercase_ReturnsFalse() {
        assertFalse(validator.isValid("p@ssword1", null));
    }

    @Test
    void isValid_MissingLowercase_ReturnsFalse() {
        assertFalse(validator.isValid("P@SSWORD1", null));
    }

    @Test
    void isValid_MissingDigit_ReturnsFalse() {
        assertFalse(validator.isValid("P@ssword", null));
    }

    @Test
    void isValid_MissingSpecialChar_ReturnsFalse() {
        assertFalse(validator.isValid("Password123", null));
    }
}
