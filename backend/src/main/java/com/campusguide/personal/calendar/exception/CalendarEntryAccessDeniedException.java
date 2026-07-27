package com.campusguide.personal.calendar.exception;

import org.springframework.security.access.AccessDeniedException;

public class CalendarEntryAccessDeniedException extends AccessDeniedException {
    public CalendarEntryAccessDeniedException(String message) {
        super(message);
    }
}
