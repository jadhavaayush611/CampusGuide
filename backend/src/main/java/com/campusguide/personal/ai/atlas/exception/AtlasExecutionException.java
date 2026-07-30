package com.campusguide.personal.ai.atlas.exception;

public class AtlasExecutionException extends AtlasException {

    public AtlasExecutionException(String message) {
        super(message, AtlasErrorCategory.EXECUTION_FAILURE);
    }

    public AtlasExecutionException(String message, Throwable cause) {
        super(message, cause, AtlasErrorCategory.EXECUTION_FAILURE);
    }
}
