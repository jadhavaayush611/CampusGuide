package com.campusguide.personal.notification.exception;

public class ScheduledNotificationAccessDeniedException extends RuntimeException {
    public ScheduledNotificationAccessDeniedException(String message) {
        super(message);
    }
}
