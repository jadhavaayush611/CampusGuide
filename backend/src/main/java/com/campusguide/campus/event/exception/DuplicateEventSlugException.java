package com.campusguide.campus.event.exception;

import com.campusguide.common.exception.ConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEventSlugException extends ConflictException {

    public DuplicateEventSlugException(String message) {
        super(message);
    }
}
