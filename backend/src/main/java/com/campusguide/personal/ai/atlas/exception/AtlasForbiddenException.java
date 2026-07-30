package com.campusguide.personal.ai.atlas.exception;

public class AtlasForbiddenException extends AtlasException {

    public AtlasForbiddenException(String message) {
        super(message, AtlasErrorCategory.AUTHORIZATION);
    }

    public AtlasForbiddenException(String message, Throwable cause) {
        super(message, cause, AtlasErrorCategory.AUTHORIZATION);
    }
}
