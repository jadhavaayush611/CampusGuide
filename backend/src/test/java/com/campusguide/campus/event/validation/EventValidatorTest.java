package com.campusguide.campus.event.validation;

import com.campusguide.campus.event.exception.InvalidEventDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventValidatorTest {

    private EventValidator eventValidator;

    @BeforeEach
    void setUp() {
        eventValidator = new EventValidator();
    }

    @Test
    void validate_ValidInput_DoesNotThrow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime regStart = now.plusDays(1);
        LocalDateTime regEnd = now.plusDays(3);
        LocalDateTime startTime = now.plusDays(5);
        LocalDateTime endTime = now.plusDays(6);

        assertDoesNotThrow(() -> eventValidator.validate(
                startTime, endTime, true, regStart, regEnd, 100
        ));
    }

    @Test
    void validate_NullStartTimeOrEndTime_ThrowsInvalidEventDataException() {
        LocalDateTime now = LocalDateTime.now();
        assertThrows(InvalidEventDataException.class, () ->
                eventValidator.validate(null, now.plusDays(1), false, null, null, null)
        );
        assertThrows(InvalidEventDataException.class, () ->
                eventValidator.validate(now, null, false, null, null, null)
        );
    }

    @Test
    void validate_StartTimeNotBeforeEndTime_ThrowsInvalidEventDataException() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusDays(5);
        LocalDateTime endTime = now.plusDays(4); // invalid

        InvalidEventDataException ex = assertThrows(InvalidEventDataException.class, () ->
                eventValidator.validate(startTime, endTime, false, null, null, null)
        );
        assertEquals("Start time must be before end time", ex.getMessage());
    }

    @Test
    void validate_RegistrationStartAfterEnd_ThrowsInvalidEventDataException() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime regStart = now.plusDays(4);
        LocalDateTime regEnd = now.plusDays(3); // invalid
        LocalDateTime startTime = now.plusDays(5);
        LocalDateTime endTime = now.plusDays(6);

        InvalidEventDataException ex = assertThrows(InvalidEventDataException.class, () ->
                eventValidator.validate(startTime, endTime, true, regStart, regEnd, null)
        );
        assertEquals("Registration start time must be before registration end time", ex.getMessage());
    }

    @Test
    void validate_RegistrationEndNotBeforeStartTime_ThrowsInvalidEventDataException() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime regStart = now.plusDays(1);
        LocalDateTime regEnd = now.plusDays(6); // closes after startTime
        LocalDateTime startTime = now.plusDays(5);
        LocalDateTime endTime = now.plusDays(7);

        InvalidEventDataException ex = assertThrows(InvalidEventDataException.class, () ->
                eventValidator.validate(startTime, endTime, true, regStart, regEnd, null)
        );
        assertEquals("Registration closes before event begins", ex.getMessage());
    }

    @Test
    void validate_NegativeOrZeroCapacity_ThrowsInvalidEventDataException() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusDays(2);
        LocalDateTime endTime = now.plusDays(3);

        assertThrows(InvalidEventDataException.class, () ->
                eventValidator.validate(startTime, endTime, false, null, null, 0)
        );
        assertThrows(InvalidEventDataException.class, () ->
                eventValidator.validate(startTime, endTime, false, null, null, -10)
        );
    }
}
