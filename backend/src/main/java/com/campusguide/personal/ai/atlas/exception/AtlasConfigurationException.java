package com.campusguide.personal.ai.atlas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AtlasConfigurationException extends AtlasException {

    public AtlasConfigurationException(String message) {
        super(message, AtlasErrorCategory.SYSTEM_ERROR);
    }

    public AtlasConfigurationException(String message, Throwable cause) {
        super(message, cause, AtlasErrorCategory.SYSTEM_ERROR);
    }
}
