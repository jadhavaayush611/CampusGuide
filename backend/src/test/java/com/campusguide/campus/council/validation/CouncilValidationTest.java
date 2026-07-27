package com.campusguide.campus.council.validation;

import com.campusguide.campus.council.dto.CreateCouncilRequest;
import com.campusguide.campus.council.dto.UpdateCouncilStatusRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CouncilValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testValidCreateCouncilRequest_PassesValidation() {
        CreateCouncilRequest request = CreateCouncilRequest.builder()
                .name("Literary Society")
                .slug("literary-society")
                .description("Promoting literature, creative writing, and debate.")
                .email("literary@campus.edu")
                .isActive(true)
                .build();

        Set<ConstraintViolation<CreateCouncilRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    void testBlankName_FailsValidation() {
        CreateCouncilRequest request = CreateCouncilRequest.builder()
                .name("  ")
                .slug("valid-slug")
                .description("A valid description")
                .build();

        Set<ConstraintViolation<CreateCouncilRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid-slug", "gdsc-123", "sports", "tech-council-2026"})
    void testValidSlugs_PassValidation(String slug) {
        CreateCouncilRequest request = CreateCouncilRequest.builder()
                .name("Valid Name")
                .slug(slug)
                .description("A valid description")
                .build();

        Set<ConstraintViolation<CreateCouncilRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Slug '" + slug + "' should be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Invalid Slug!", "slug_with_underscore", "slug--double-hyphen", "-leading-hyphen", "trailing-hyphen-", "UPPERCASE-SLUG"})
    void testInvalidSlugs_FailValidation(String slug) {
        CreateCouncilRequest request = CreateCouncilRequest.builder()
                .name("Valid Name")
                .slug(slug)
                .description("A valid description")
                .build();

        Set<ConstraintViolation<CreateCouncilRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Slug '" + slug + "' should be invalid");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("slug")));
    }

    @Test
    void testInvalidEmail_FailsValidation() {
        CreateCouncilRequest request = CreateCouncilRequest.builder()
                .name("Valid Name")
                .slug("valid-slug")
                .description("A valid description")
                .email("not-an-email")
                .build();

        Set<ConstraintViolation<CreateCouncilRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void testNullStatusInStatusRequest_FailsValidation() {
        UpdateCouncilStatusRequest request = new UpdateCouncilStatusRequest(null);
        Set<ConstraintViolation<UpdateCouncilStatusRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("isActive")));
    }
}
