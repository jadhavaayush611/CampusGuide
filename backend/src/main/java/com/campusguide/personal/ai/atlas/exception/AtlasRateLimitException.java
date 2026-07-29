package com.campusguide.personal.ai.atlas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class AtlasRateLimitException extends AtlasException {

    public AtlasRateLimitException(String message) {
        super(message, AtlasErrorCategory.RATE_LIMIT);
    }

    public AtlasRateLimitException(String message, Throwable cause) {
        super(message, cause, AtlasErrorCategory.RATE_LIMIT);
    }
}
