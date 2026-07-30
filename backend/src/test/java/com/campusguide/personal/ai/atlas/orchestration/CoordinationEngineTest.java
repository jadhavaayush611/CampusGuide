package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.orchestration.coordination.CoordinationEngine;
import com.campusguide.personal.ai.atlas.orchestration.coordination.CoordinationPolicy;
import com.campusguide.personal.ai.atlas.orchestration.coordination.SynchronizationBarrier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CoordinationEngineTest {

    private CoordinationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new CoordinationEngine();
    }

    @Test
    void testSynchronizationBarrierArrival() {
        SynchronizationBarrier barrier = engine.createBarrier(2);

        assertFalse(barrier.isReleased());

        barrier.arrive("agent_1", Map.of("out1", "data1"));
        assertFalse(barrier.isReleased());

        barrier.arrive("agent_2", Map.of("out2", "data2"));
        assertTrue(barrier.isReleased());
        assertEquals(2, barrier.getArrivedResults().size());
    }

    @Test
    void testResultMergerAndConflictResolution() {
        Map<String, Object> r1 = Map.of("k1", "v1", "k2", "v2");
        Map<String, Object> r2 = Map.of("k2", "v2_override", "k3", "v3");

        CoordinationPolicy policy = CoordinationPolicy.builder()
                .conflictPolicy(CoordinationPolicy.ConflictResolutionPolicy.LATEST_WINS)
                .build();

        Map<String, Object> merged = engine.mergeResults(r1, policy);
        merged = engine.mergeResults(merged, r2, policy);

        assertEquals("v1", merged.get("k1"));
        assertEquals("v2_override", merged.get("k2"));
        assertEquals("v3", merged.get("k3"));
    }
}
