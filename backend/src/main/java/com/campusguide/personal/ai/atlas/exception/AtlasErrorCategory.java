package com.campusguide.personal.ai.atlas.exception;

public enum AtlasErrorCategory {
    VALIDATION,
    AUTHENTICATION,
    RATE_LIMIT,
    TIMEOUT,
    PROVIDER_TRANSIENT,
    PROVIDER_PERMANENT,
    CIRCUIT_BREAKER_OPEN,
    SYSTEM_ERROR
}
