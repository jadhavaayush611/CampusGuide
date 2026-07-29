package com.campusguide.personal.ai.atlas.resilience;

import com.campusguide.personal.ai.atlas.config.AtlasProperties;
import com.campusguide.personal.ai.atlas.metrics.AtlasMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CircuitBreakerTest {

    @Mock
    private AtlasMetrics atlasMetrics;

    private AtlasProperties atlasProperties;
    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        atlasProperties = new AtlasProperties();
        atlasProperties.getCircuitBreaker().setFailureThreshold(2);
        atlasProperties.getCircuitBreaker().setWaitDurationInOpenStateMs(100);
        atlasProperties.getCircuitBreaker().setPermittedNumberOfCallsInHalfOpenState(2);
        circuitBreaker = new CircuitBreaker(atlasProperties, atlasMetrics);
    }

    @Test
    void testStateTransitions() throws InterruptedException {
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        assertTrue(circuitBreaker.allowRequest());

        // 1 failure -> still CLOSED
        circuitBreaker.recordFailure();
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());

        // 2 failures -> OPEN
        circuitBreaker.recordFailure();
        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        assertFalse(circuitBreaker.allowRequest());

        // Wait for waitDurationInOpenStateMs
        Thread.sleep(150);

        // Next request transitions to HALF_OPEN
        assertTrue(circuitBreaker.allowRequest());
        assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreaker.getState());

        // 2 successful calls in HALF_OPEN -> CLOSED
        circuitBreaker.recordSuccess();
        circuitBreaker.recordSuccess();
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }
}
