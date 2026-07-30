package com.campusguide.personal.ai.atlas.execution.runtime.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculates retry decisions, backoff delays, circuit breaker states, and manages attempt counts for execution units.
 */
@Slf4j
@Component
public class RetryEngine {

    private final Map<String, Integer> failureCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> circuitBreakerOpenTimes = new ConcurrentHashMap<>();
    private static final long CIRCUIT_BREAKER_RESET_MS = 60000L; // 1 min

    public RetryDecision evaluateRetry(String unitId, int currentAttempt, RetryPolicy policy, Throwable exception) {
        if (policy == null || policy.getStrategy() == RetryStrategy.NO_RETRY || policy.getMaxRetries() <= 0) {
            return RetryDecision.noRetry(currentAttempt, "Retry policy set to NO_RETRY or maxRetries <= 0");
        }

        if (currentAttempt >= policy.getMaxRetries()) {
            return RetryDecision.noRetry(currentAttempt, "Max retries reached (" + policy.getMaxRetries() + ")");
        }

        int totalFailures = failureCounts.merge(unitId, 1, Integer::sum);

        if (policy.getStrategy() == RetryStrategy.CIRCUIT_BREAKER) {
            Long openTime = circuitBreakerOpenTimes.get(unitId);
            if (openTime != null && (System.currentTimeMillis() - openTime) < CIRCUIT_BREAKER_RESET_MS) {
                log.warn("Circuit breaker OPEN for unitId {}", unitId);
                return RetryDecision.noRetry(currentAttempt, "Circuit breaker is OPEN");
            } else if (openTime != null) {
                // Half-open / reset
                circuitBreakerOpenTimes.remove(unitId);
            }

            if (totalFailures >= 5) {
                circuitBreakerOpenTimes.put(unitId, System.currentTimeMillis());
                log.error("Circuit breaker TRIPPED for unitId {} after {} failures", unitId, totalFailures);
                return RetryDecision.noRetry(currentAttempt, "Circuit breaker tripped due to consecutive failures");
            }
        }

        long backoffMs = calculateBackoff(currentAttempt, policy);
        return RetryDecision.retry(currentAttempt + 1, backoffMs, policy.getStrategy(),
                "Retry attempt " + (currentAttempt + 1) + "/" + policy.getMaxRetries() + " with " + policy.getStrategy());
    }

    private long calculateBackoff(int attempt, RetryPolicy policy) {
        switch (policy.getStrategy()) {
            case IMMEDIATE:
                return 0L;
            case FIXED:
                return Math.min(policy.getInitialBackoffMs(), policy.getMaxBackoffMs());
            case EXPONENTIAL_BACKOFF:
            case CIRCUIT_BREAKER:
            default:
                long calculated = (long) (policy.getInitialBackoffMs() * Math.pow(policy.getBackoffMultiplier(), attempt));
                return Math.min(calculated, policy.getMaxBackoffMs());
        }
    }

    public void resetFailureCount(String unitId) {
        if (unitId != null) {
            failureCounts.remove(unitId);
            circuitBreakerOpenTimes.remove(unitId);
        }
    }
}
