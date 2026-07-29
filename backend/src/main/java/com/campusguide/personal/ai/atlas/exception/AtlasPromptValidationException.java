package com.campusguide.personal.ai.atlas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AtlasPromptValidationException extends AtlasException {

    public AtlasPromptValidationException(String message) {
        super(message, AtlasErrorCategory.VALIDATION);
    }

    public AtlasPromptValidationException(String message, Throwable cause) {
        super(message, cause, AtlasErrorCategory.VALIDATION);
    }
}
