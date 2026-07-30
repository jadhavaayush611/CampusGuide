package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.orchestration.registry.AgentCapability;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentMetadata;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgentRegistryTest {

    private AgentRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new AgentRegistry();
    }

    @Test
    void testDynamicRegistrationAndDiscovery() {
        AgentDescriptor descriptor = AgentDescriptor.builder()
                .agentId("agent_planner_1")
                .name("Planner Agent")
                .agentType("PLANNER")
                .capabilities(List.of(AgentCapability.of("TaskScheduling", "planner")))
                .metadata(AgentMetadata.defaultFor("Task Scheduling", "planner"))
                .build();

        registry.registerAgent(descriptor);

        assertTrue(registry.getDescriptor("agent_planner_1").isPresent());
        List<AgentDescriptor> byCap = registry.findAgentsByCapability("TaskScheduling");
        assertEquals(1, byCap.size());
        assertEquals("agent_planner_1", byCap.get(0).getAgentId());

        List<AgentDescriptor> byDomain = registry.findAgentsByDomain("planner");
        assertEquals(1, byDomain.size());
    }

    @Test
    void testHealthStatusAndLoadUpdate() {
        AgentDescriptor descriptor = AgentDescriptor.builder()
                .agentId("agent_campus_1")
                .healthStatus(AgentDescriptor.HealthStatus.HEALTHY)
                .build();

        registry.registerAgent(descriptor);

        registry.updateHealthStatus("agent_campus_1", AgentDescriptor.HealthStatus.UNHEALTHY);
        assertEquals(AgentDescriptor.HealthStatus.UNHEALTHY, registry.getDescriptor("agent_campus_1").get().getHealthStatus());
        assertFalse(registry.getDescriptor("agent_campus_1").get().isAvailable());

        registry.updateLoad("agent_campus_1", 3);
        assertEquals(3, registry.getDescriptor("agent_campus_1").get().getCurrentLoad());
    }
}
