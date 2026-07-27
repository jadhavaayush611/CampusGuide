package com.campusguide.personal.calendar.exception;

import com.campusguide.common.exception.BadRequestException;

public class CalendarEntryValidationException extends BadRequestException {
    public CalendarEntryValidationException(String message) {
        super(message);
    }
}
