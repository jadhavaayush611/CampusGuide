package com.campusguide.personal.ai.atlas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AtlasAuthenticationException extends AtlasException {

    public AtlasAuthenticationException(String message) {
        super(message, AtlasErrorCategory.AUTHENTICATION);
    }

    public AtlasAuthenticationException(String message, Throwable cause) {
        super(message, cause, AtlasErrorCategory.AUTHENTICATION);
    }
}
