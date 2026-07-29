package com.campusguide.personal.ai.atlas.validation;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.dto.AtlasChatRequest;
import com.campusguide.personal.ai.atlas.exception.AtlasPromptValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtlasPromptValidatorTest {

    private AtlasProperties properties;
    private AtlasPromptValidator validator;

    @BeforeEach
    void setUp() {
        properties = new AtlasProperties();
        properties.setMaxPromptLength(100);
        validator = new AtlasPromptValidator(properties);
    }

    @Test
    void testValidateRequest_Valid() {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("Short valid prompt")
                .build();
        assertDoesNotThrow(() -> validator.validateRequest(request));
    }

    @Test
    void testValidateRequest_NullRequest() {
        assertThrows(AtlasPromptValidationException.class, () -> validator.validateRequest(null));
    }

    @Test
    void testValidateRequest_EmptyPrompt() {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("   ")
                .build();
        assertThrows(AtlasPromptValidationException.class, () -> validator.validateRequest(request));
    }

    @Test
    void testValidateRequest_ExceedsMaxLength() {
        String longPrompt = "a".repeat(101);
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt(longPrompt)
                .build();
        assertThrows(AtlasPromptValidationException.class, () -> validator.validateRequest(request));
    }

    @Test
    void testValidateRequest_InvalidTemperature() {
        AtlasChatRequest request = AtlasChatRequest.builder()
                .prompt("Valid prompt")
                .temperature(3.0)
                .build();
        assertThrows(AtlasPromptValidationException.class, () -> validator.validateRequest(request));
    }
}
