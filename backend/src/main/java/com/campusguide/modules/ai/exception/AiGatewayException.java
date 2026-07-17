package com.campusguide.modules.ai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class AiGatewayException extends RuntimeException {
    public AiGatewayException(String message) {
        super(message);
    }

    public AiGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
