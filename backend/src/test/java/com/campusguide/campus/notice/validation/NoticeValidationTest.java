package com.campusguide.campus.notice.validation;

import com.campusguide.campus.notice.dto.CreateNoticeRequest;
import com.campusguide.campus.notice.dto.UpdateNoticeRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NoticeValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createNoticeRequest_ValidRequest_PassesValidation() {
        CreateNoticeRequest request = CreateNoticeRequest.builder()
                .title("Valid Notice Title")
                .slug("valid-notice-slug-123")
                .content("Valid content for the notice.")
                .build();

        Set<ConstraintViolation<CreateNoticeRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void createNoticeRequest_BlankTitle_FailsValidation() {
        CreateNoticeRequest request = CreateNoticeRequest.builder()
                .title("   ")
                .slug("valid-slug")
                .content("Valid content.")
                .build();

        Set<ConstraintViolation<CreateNoticeRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void createNoticeRequest_BlankContent_FailsValidation() {
        CreateNoticeRequest request = CreateNoticeRequest.builder()
                .title("Valid Title")
                .slug("valid-slug")
                .content("")
                .build();

        Set<ConstraintViolation<CreateNoticeRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("content")));
    }

    @Test
    void createNoticeRequest_InvalidSlug_FailsValidation() {
        CreateNoticeRequest request = CreateNoticeRequest.builder()
                .title("Valid Title")
                .slug("Invalid Slug With Spaces!")
                .content("Valid content.")
                .build();

        Set<ConstraintViolation<CreateNoticeRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("slug")));
    }

    @Test
    void updateNoticeRequest_ValidRequest_PassesValidation() {
        UpdateNoticeRequest request = UpdateNoticeRequest.builder()
                .title("Updated Title")
                .slug("updated-slug-456")
                .content("Updated content.")
                .build();

        Set<ConstraintViolation<UpdateNoticeRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void slugValidator_DirectTest() {
        SlugValidator slugValidator = new SlugValidator();

        assertTrue(slugValidator.isValid(null, null));
        assertTrue(slugValidator.isValid("", null));
        assertTrue(slugValidator.isValid("valid-slug", null));
        assertTrue(slugValidator.isValid("slug123", null));

        assertFalse(slugValidator.isValid("Invalid Slug", null));
        assertFalse(slugValidator.isValid("slug_underscore", null));
        assertFalse(slugValidator.isValid("-leading-hyphen", null));
        assertFalse(slugValidator.isValid("trailing-hyphen-", null));
    }
}
