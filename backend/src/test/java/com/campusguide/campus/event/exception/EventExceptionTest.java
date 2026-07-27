package com.campusguide.campus.event.exception;

import com.campusguide.common.exception.BadRequestException;
import com.campusguide.common.exception.ConflictException;
import com.campusguide.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class EventExceptionTest {

    @Test
    void eventNotFoundException_PropertiesAndHierarchy() {
        EventNotFoundException ex = new EventNotFoundException("Event missing");
        assertEquals("Event missing", ex.getMessage());
        assertTrue(ex instanceof ResourceNotFoundException);

        ResponseStatus status = EventNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(status);
        assertEquals(HttpStatus.NOT_FOUND, status.value());
    }

    @Test
    void duplicateEventSlugException_PropertiesAndHierarchy() {
        DuplicateEventSlugException ex = new DuplicateEventSlugException("Slug conflict");
        assertEquals("Slug conflict", ex.getMessage());
        assertTrue(ex instanceof ConflictException);

        ResponseStatus status = DuplicateEventSlugException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(status);
        assertEquals(HttpStatus.CONFLICT, status.value());
    }

    @Test
    void invalidEventDataException_PropertiesAndHierarchy() {
        InvalidEventDataException ex = new InvalidEventDataException("Invalid time");
        assertEquals("Invalid time", ex.getMessage());
        assertTrue(ex instanceof BadRequestException);

        ResponseStatus status = InvalidEventDataException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(status);
        assertEquals(HttpStatus.BAD_REQUEST, status.value());
    }
}
