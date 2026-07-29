package com.campusguide.personal.ai.atlas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class AtlasProviderException extends AtlasException {

    public AtlasProviderException(String message) {
        super(message, AtlasErrorCategory.PROVIDER_PERMANENT);
    }

    public AtlasProviderException(String message, Throwable cause) {
        super(message, cause, AtlasErrorCategory.PROVIDER_PERMANENT);
    }

    public AtlasProviderException(String message, AtlasErrorCategory category) {
        super(message, category);
    }

    public AtlasProviderException(String message, Throwable cause, AtlasErrorCategory category) {
        super(message, cause, category);
    }
}
