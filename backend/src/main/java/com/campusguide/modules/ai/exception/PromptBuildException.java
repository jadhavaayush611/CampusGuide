package com.campusguide.modules.ai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PromptBuildException extends RuntimeException {
    public PromptBuildException(String message) {
        super(message);
    }

    public PromptBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
