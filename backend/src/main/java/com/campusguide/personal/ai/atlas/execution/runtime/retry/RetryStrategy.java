package com.campusguide.personal.ai.atlas.execution.runtime.retry;

/**
 * Strategy for retry attempts upon execution unit or tool failure.
 */
public enum RetryStrategy {
    FIXED,
    EXPONENTIAL_BACKOFF,
    CIRCUIT_BREAKER,
    IMMEDIATE,
    NO_RETRY
}
