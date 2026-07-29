package com.campusguide.personal.ai.atlas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
public class AtlasTimeoutException extends AtlasException {

    public AtlasTimeoutException(String message) {
        super(message, AtlasErrorCategory.TIMEOUT);
    }

    public AtlasTimeoutException(String message, Throwable cause) {
        super(message, cause, AtlasErrorCategory.TIMEOUT);
    }
}
