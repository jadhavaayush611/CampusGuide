package com.campusguide.campus.council.exception;

import com.campusguide.common.exception.ConflictException;
import com.campusguide.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class CouncilExceptionTest {

    @Test
    void testCouncilNotFoundException_InheritanceAndResponseStatus() {
        CouncilNotFoundException ex = new CouncilNotFoundException("Council not found");
        assertTrue(ex instanceof ResourceNotFoundException);
        assertEquals("Council not found", ex.getMessage());

        ResponseStatus annotation = CouncilNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.NOT_FOUND, annotation.value());
    }

    @Test
    void testDuplicateCouncilException_InheritanceAndResponseStatus() {
        DuplicateCouncilException ex = new DuplicateCouncilException("Duplicate council name");
        assertTrue(ex instanceof ConflictException);
        assertEquals("Duplicate council name", ex.getMessage());

        ResponseStatus annotation = DuplicateCouncilException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.CONFLICT, annotation.value());
    }

    @Test
    void testCouncilHasDependenciesException_InheritanceAndResponseStatus() {
        CouncilHasDependenciesException ex = new CouncilHasDependenciesException("Dependent entities exist");
        assertTrue(ex instanceof ConflictException);
        assertEquals("Dependent entities exist", ex.getMessage());

        ResponseStatus annotation = CouncilHasDependenciesException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.CONFLICT, annotation.value());
    }
}
