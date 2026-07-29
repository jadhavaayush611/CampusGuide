package com.campusguide.personal.ai.atlas.exception;

import lombok.Getter;

@Getter
public abstract class AtlasException extends RuntimeException {

    private final AtlasErrorCategory category;

    public AtlasException(String message, AtlasErrorCategory category) {
        super(message);
        this.category = category;
    }

    public AtlasException(String message, Throwable cause, AtlasErrorCategory category) {
        super(message, cause);
        this.category = category;
    }
}
