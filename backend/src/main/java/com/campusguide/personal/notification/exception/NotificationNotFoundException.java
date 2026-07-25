package com.campusguide.personal.notification.exception;

import com.campusguide.common.exception.ResourceNotFoundException;

public class NotificationNotFoundException extends ResourceNotFoundException {
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
