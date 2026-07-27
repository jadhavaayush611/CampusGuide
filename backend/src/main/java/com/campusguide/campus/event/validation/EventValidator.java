package com.campusguide.campus.event.validation;

import com.campusguide.campus.event.exception.InvalidEventDataException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventValidator {

    public void validate(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Boolean registrationRequired,
            LocalDateTime registrationStart,
            LocalDateTime registrationEnd,
            Integer capacity) {

        if (startTime == null || endTime == null) {
            throw new InvalidEventDataException("Start time and end time are required");
        }

        if (!startTime.isBefore(endTime)) {
            throw new InvalidEventDataException("Start time must be before end time");
        }

        if (registrationStart != null && registrationEnd != null) {
            if (!registrationStart.isBefore(registrationEnd)) {
                throw new InvalidEventDataException("Registration start time must be before registration end time");
            }
        }

        if (registrationEnd != null) {
            if (!registrationEnd.isBefore(startTime)) {
                throw new InvalidEventDataException("Registration closes before event begins");
            }
        }

        if (capacity != null && capacity <= 0) {
            throw new InvalidEventDataException("Capacity must be positive");
        }
    }
}
