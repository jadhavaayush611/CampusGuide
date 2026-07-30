package com.campusguide.personal.ai.atlas.execution.runtime.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryEngineTest {

    private RetryEngine retryEngine;

    @BeforeEach
    void setUp() {
        retryEngine = new RetryEngine();
    }

    @Test
    void testExponentialBackoffRetryDecision() {
        RetryPolicy policy = RetryPolicy.defaultConfig(); // maxRetries 3

        RetryDecision decision1 = retryEngine.evaluateRetry("unit_1", 0, policy, null);
        assertTrue(decision1.isShouldRetry());
        assertEquals(1, decision1.getAttemptNumber());
        assertTrue(decision1.getBackoffMs() > 0);

        RetryDecision decisionMax = retryEngine.evaluateRetry("unit_1", 3, policy, null);
        assertFalse(decisionMax.isShouldRetry());
    }

    @Test
    void testNoRetryPolicy() {
        RetryPolicy policy = RetryPolicy.noRetry();
        RetryDecision decision = retryEngine.evaluateRetry("unit_2", 0, policy, null);
        assertFalse(decision.isShouldRetry());
    }

    @Test
    void testCircuitBreakerTripsAfterFailures() {
        RetryPolicy policy = RetryPolicy.builder()
                .maxRetries(10)
                .strategy(RetryStrategy.CIRCUIT_BREAKER)
                .build();

        for (int i = 0; i < 4; i++) {
            retryEngine.evaluateRetry("unit_cb", i, policy, null);
        }

        // 5th failure triggers circuit breaker
        RetryDecision decision = retryEngine.evaluateRetry("unit_cb", 5, policy, null);
        assertFalse(decision.isShouldRetry());
        assertTrue(decision.getReason().toLowerCase().contains("circuit breaker"));
    }
}
