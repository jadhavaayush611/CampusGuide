package com.campusguide.campus.council.exception;

import com.campusguide.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CouncilNotFoundException extends ResourceNotFoundException {

    public CouncilNotFoundException(String message) {
        super(message);
    }
}
