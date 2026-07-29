package com.campusguide.personal.ai.atlas.resilience;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.metrics.AtlasMetrics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class CircuitBreaker {

    public enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private final AtlasProperties atlasProperties;
    private final AtlasMetrics atlasMetrics;

    @Getter
    private volatile State state = State.CLOSED;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicInteger halfOpenSuccessfulCalls = new AtomicInteger(0);
    private volatile long lastStateChangeTimestamp = System.currentTimeMillis();

    public CircuitBreaker(AtlasProperties atlasProperties, AtlasMetrics atlasMetrics) {
        this.atlasProperties = atlasProperties;
        this.atlasMetrics = atlasMetrics;
    }

    public synchronized boolean allowRequest() {
        if (!atlasProperties.getCircuitBreaker().isEnabled()) {
            return true;
        }

        long now = System.currentTimeMillis();

        if (state == State.OPEN) {
            long waitDuration = atlasProperties.getCircuitBreaker().getWaitDurationInOpenStateMs();
            if (now - lastStateChangeTimestamp >= waitDuration) {
                transitionTo(State.HALF_OPEN);
                halfOpenSuccessfulCalls.set(0);
                return true;
            }
            return false;
        }

        return true; // CLOSED or HALF_OPEN
    }

    public synchronized void recordSuccess() {
        if (!atlasProperties.getCircuitBreaker().isEnabled()) {
            return;
        }

        consecutiveFailures.set(0);
        if (state == State.HALF_OPEN) {
            int successes = halfOpenSuccessfulCalls.incrementAndGet();
            if (successes >= atlasProperties.getCircuitBreaker().getPermittedNumberOfCallsInHalfOpenState()) {
                transitionTo(State.CLOSED);
            }
        }
    }

    public synchronized void recordFailure() {
        if (!atlasProperties.getCircuitBreaker().isEnabled()) {
            return;
        }

        int failures = consecutiveFailures.incrementAndGet();
        if (state == State.HALF_OPEN) {
            transitionTo(State.OPEN);
        } else if (state == State.CLOSED && failures >= atlasProperties.getCircuitBreaker().getFailureThreshold()) {
            transitionTo(State.OPEN);
        }
    }

    public synchronized void reset() {
        consecutiveFailures.set(0);
        halfOpenSuccessfulCalls.set(0);
        transitionTo(State.CLOSED);
    }

    private void transitionTo(State newState) {
        if (this.state != newState) {
            log.info("Circuit breaker state transition: {} -> {}", this.state, newState);
            this.state = newState;
            this.lastStateChangeTimestamp = System.currentTimeMillis();
            if (atlasMetrics != null) {
                atlasMetrics.recordCircuitBreakerEvent("OpenAI", newState.name());
            }
        }
    }
}
