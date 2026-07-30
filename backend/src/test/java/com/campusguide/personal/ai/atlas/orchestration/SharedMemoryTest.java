package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.orchestration.memory.MemoryCoordinator;
import com.campusguide.personal.ai.atlas.orchestration.memory.MemoryLease;
import com.campusguide.personal.ai.atlas.orchestration.memory.SharedMemory;
import com.campusguide.personal.ai.atlas.orchestration.memory.SharedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SharedMemoryTest {

    private SharedMemory memory;
    private MemoryCoordinator coordinator;

    @BeforeEach
    void setUp() {
        memory = new SharedMemory();
        coordinator = new MemoryCoordinator(memory);
    }

    @Test
    void testSharedStatePutGetVersion() {
        memory.put("workflow_100", "state_key", "value1", "agent_1");

        Optional<SharedState> stateOpt = memory.get("workflow_100", "state_key");
        assertTrue(stateOpt.isPresent());
        assertEquals("value1", stateOpt.get().getValue());
        assertEquals(1L, stateOpt.get().getVersion());

        memory.put("workflow_100", "state_key", "value2", "agent_2");
        assertEquals("value2", memory.get("workflow_100", "state_key").get().getValue());
        assertEquals(2L, memory.get("workflow_100", "state_key").get().getVersion());
    }

    @Test
    void testLeaseAcquisitionAndRelease() {
        Optional<MemoryLease> lease1 = coordinator.acquireLease("workflow_100", "res_1", "agent_1", 10000L);
        assertTrue(lease1.isPresent());

        // Agent 2 should be rejected while agent 1 holds lease
        Optional<MemoryLease> lease2 = coordinator.acquireLease("workflow_100", "res_1", "agent_2", 10000L);
        assertTrue(lease2.isEmpty());

        boolean updated = coordinator.updateStateWithLease(lease1.get(), "leased_value");
        assertTrue(updated);
        assertEquals("leased_value", memory.get("workflow_100", "res_1").get().getValue());

        boolean released = coordinator.releaseLease(lease1.get());
        assertTrue(released);

        // Agent 2 can now acquire lease
        Optional<MemoryLease> lease2Retry = coordinator.acquireLease("workflow_100", "res_1", "agent_2", 10000L);
        assertTrue(lease2Retry.isPresent());
    }
}
