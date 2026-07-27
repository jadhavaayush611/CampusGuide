package com.campusguide.personal.achievement.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AchievementExceptionTest {

    @Test
    void testAchievementNotFoundException() {
        AchievementNotFoundException ex = new AchievementNotFoundException("Achievement not found");
        assertNotNull(ex);
        assertEquals("Achievement not found", ex.getMessage());
    }

    @Test
    void testAchievementAccessDeniedException() {
        AchievementAccessDeniedException ex = new AchievementAccessDeniedException("Access denied");
        assertNotNull(ex);
        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void testAchievementAlreadyExistsException() {
        AchievementAlreadyExistsException ex = new AchievementAlreadyExistsException("Achievement already exists");
        assertNotNull(ex);
        assertEquals("Achievement already exists", ex.getMessage());
    }

    @Test
    void testAchievementValidationException() {
        AchievementValidationException ex = new AchievementValidationException("Validation failed");
        assertNotNull(ex);
        assertEquals("Validation failed", ex.getMessage());
    }
}
