package com.campusguide.personal.ai.atlas.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AtlasExceptionTest {

    @Test
    void testAtlasPromptValidationException() {
        AtlasPromptValidationException ex = new AtlasPromptValidationException("Validation failed");
        assertEquals("Validation failed", ex.getMessage());

        RuntimeException cause = new RuntimeException("root cause");
        AtlasPromptValidationException ex2 = new AtlasPromptValidationException("Validation failed", cause);
        assertEquals(cause, ex2.getCause());
    }

    @Test
    void testAtlasProviderUnavailableException() {
        AtlasProviderUnavailableException ex = new AtlasProviderUnavailableException("Provider down");
        assertEquals("Provider down", ex.getMessage());
    }

    @Test
    void testAtlasTimeoutException() {
        AtlasTimeoutException ex = new AtlasTimeoutException("Timeout occurred");
        assertEquals("Timeout occurred", ex.getMessage());
    }

    @Test
    void testAtlasProviderException() {
        AtlasProviderException ex = new AtlasProviderException("Provider error");
        assertEquals("Provider error", ex.getMessage());
    }
}
