package com.campusguide.personal.calendar.exception;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

class CalendarEntryExceptionTest {

    @Test
    void testCalendarEntryNotFoundException() {
        CalendarEntryNotFoundException ex = new CalendarEntryNotFoundException("Entry not found");
        assertEquals("Entry not found", ex.getMessage());
        assertInstanceOf(ResourceNotFoundException.class, ex);
    }

    @Test
    void testCalendarEntryAccessDeniedException() {
        CalendarEntryAccessDeniedException ex = new CalendarEntryAccessDeniedException("Access denied");
        assertEquals("Access denied", ex.getMessage());
        assertInstanceOf(AccessDeniedException.class, ex);
    }

    @Test
    void testCalendarEntryValidationException() {
        CalendarEntryValidationException ex = new CalendarEntryValidationException("Validation failed");
        assertEquals("Validation failed", ex.getMessage());
        assertInstanceOf(BadRequestException.class, ex);
    }
}
