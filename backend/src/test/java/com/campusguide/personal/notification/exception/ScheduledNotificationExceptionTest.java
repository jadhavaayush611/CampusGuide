package com.campusguide.personal.notification.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduledNotificationExceptionTest {

    @Test
    void testScheduledNotificationNotFoundException() {
        ScheduledNotificationNotFoundException ex = new ScheduledNotificationNotFoundException("Not found");
        assertEquals("Not found", ex.getMessage());
    }

    @Test
    void testScheduledNotificationAccessDeniedException() {
        ScheduledNotificationAccessDeniedException ex = new ScheduledNotificationAccessDeniedException("Access denied");
        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void testScheduledNotificationValidationException() {
        ScheduledNotificationValidationException ex = new ScheduledNotificationValidationException("Validation failed");
        assertEquals("Validation failed", ex.getMessage());
    }
}
