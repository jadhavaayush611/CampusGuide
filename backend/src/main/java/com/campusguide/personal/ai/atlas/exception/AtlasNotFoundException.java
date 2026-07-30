package com.campusguide.personal.ai.atlas.exception;

public class AtlasNotFoundException extends AtlasException {

    public AtlasNotFoundException(String message) {
        super(message, AtlasErrorCategory.NOT_FOUND);
    }

    public AtlasNotFoundException(String message, Throwable cause) {
        super(message, cause, AtlasErrorCategory.NOT_FOUND);
    }
}
