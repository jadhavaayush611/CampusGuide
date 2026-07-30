package com.campusguide.personal.ai.atlas.exception;

public enum AtlasErrorCategory {
    VALIDATION,
    AUTHENTICATION,
    AUTHORIZATION,
    NOT_FOUND,
    EXECUTION_FAILURE,
    RATE_LIMIT,
    QUOTA_EXCEEDED,
    TIMEOUT,
    PROVIDER_TRANSIENT,
    PROVIDER_PERMANENT,
    CIRCUIT_BREAKER_OPEN,
    SYSTEM_ERROR
}
