package com.campusguide.campus.council.exception;

import com.campusguide.common.exception.ConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CouncilHasDependenciesException extends ConflictException {

    public CouncilHasDependenciesException(String message) {
        super(message);
    }
}
