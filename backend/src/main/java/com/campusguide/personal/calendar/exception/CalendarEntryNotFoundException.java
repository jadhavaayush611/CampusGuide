package com.campusguide.personal.calendar.exception;

import com.campusguide.common.exception.ResourceNotFoundException;

public class CalendarEntryNotFoundException extends ResourceNotFoundException {
    public CalendarEntryNotFoundException(String message) {
        super(message);
    }
}
