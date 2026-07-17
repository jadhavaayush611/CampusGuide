package com.campusguide.modules.notification.exception;

import com.campusguide.exception.ResourceNotFoundException;

public class NotificationNotFoundException extends ResourceNotFoundException {
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
