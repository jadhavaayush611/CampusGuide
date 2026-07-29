package com.campusguide.personal.ai.atlas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class AtlasProviderUnavailableException extends AtlasException {

    public AtlasProviderUnavailableException(String message) {
        super(message, AtlasErrorCategory.PROVIDER_TRANSIENT);
    }

    public AtlasProviderUnavailableException(String message, Throwable cause) {
        super(message, cause, AtlasErrorCategory.PROVIDER_TRANSIENT);
    }

    public AtlasProviderUnavailableException(String message, AtlasErrorCategory category) {
        super(message, category);
    }

    public AtlasProviderUnavailableException(String message, Throwable cause, AtlasErrorCategory category) {
        super(message, cause, category);
    }
}
