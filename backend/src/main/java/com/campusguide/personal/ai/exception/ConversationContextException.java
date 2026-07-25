package com.campusguide.personal.ai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ConversationContextException extends RuntimeException {
    public ConversationContextException(String message) {
        super(message);
    }

    public ConversationContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
