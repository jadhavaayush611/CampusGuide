package com.campusguide.campus.event.exception;

import com.campusguide.common.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEventDataException extends BadRequestException {

    public InvalidEventDataException(String message) {
        super(message);
    }
}
