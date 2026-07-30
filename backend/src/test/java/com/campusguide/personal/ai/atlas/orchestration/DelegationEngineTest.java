package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.orchestration.delegation.AssignmentStrategy;
import com.campusguide.personal.ai.atlas.orchestration.delegation.DelegationEngine;
import com.campusguide.personal.ai.atlas.orchestration.delegation.DelegationPolicy;
import com.campusguide.personal.ai.atlas.orchestration.delegation.TaskAssignment;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentCapability;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentMetadata;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DelegationEngineTest {

    private AgentRegistry registry;
    private DelegationEngine delegationEngine;

    @BeforeEach
    void setUp() {
        registry = new AgentRegistry();
        delegationEngine = new DelegationEngine(registry);

        AgentDescriptor agent1 = AgentDescriptor.builder()
                .agentId("agent_1")
                .name("Academic Agent 1")
                .agentType("ACADEMIC")
                .currentLoad(2)
                .maxCapacity(10)
                .healthStatus(AgentDescriptor.HealthStatus.HEALTHY)
                .capabilities(List.of(AgentCapability.of("CourseQuery", "academic")))
                .metadata(AgentMetadata.defaultFor("Academic", "academic"))
                .build();

        AgentDescriptor agent2 = AgentDescriptor.builder()
                .agentId("agent_2")
                .name("Academic Agent 2")
                .agentType("ACADEMIC")
                .currentLoad(0)
                .maxCapacity(10)
                .healthStatus(AgentDescriptor.HealthStatus.HEALTHY)
                .capabilities(List.of(AgentCapability.of("CourseQuery", "academic")))
                .metadata(AgentMetadata.defaultFor("Academic", "academic"))
                .build();

        registry.registerAgent(agent1);
        registry.registerAgent(agent2);
    }

    @Test
    void testLoadBalancedDelegation() {
        DelegationPolicy policy = DelegationPolicy.builder()
                .strategy(AssignmentStrategy.LOAD_BALANCED)
                .build();

        Optional<TaskAssignment> assignmentOpt = delegationEngine.delegateTask("task_100", "CourseQuery", 5, null, policy);

        assertTrue(assignmentOpt.isPresent());
        TaskAssignment assignment = assignmentOpt.get();
        assertEquals("agent_2", assignment.getAgentId()); // lowest loaded agent selected
        assertEquals(1, registry.getDescriptor("agent_2").get().getCurrentLoad());
    }

    @Test
    void testLocalityAwareDelegation() {
        AgentDescriptor agent3 = AgentDescriptor.builder()
                .agentId("agent_3")
                .currentLoad(1)
                .maxCapacity(10)
                .healthStatus(AgentDescriptor.HealthStatus.HEALTHY)
                .capabilities(List.of(AgentCapability.of("CourseQuery", "campus")))
                .metadata(AgentMetadata.defaultFor("Campus Nav", "campus"))
                .build();
        registry.registerAgent(agent3);

        DelegationPolicy policy = DelegationPolicy.builder()
                .strategy(AssignmentStrategy.LOCALITY_AWARE)
                .build();

        Optional<TaskAssignment> assignmentOpt = delegationEngine.delegateTask("task_200", "CourseQuery", 5, "campus", policy);

        assertTrue(assignmentOpt.isPresent());
        assertEquals("agent_3", assignmentOpt.get().getAgentId());
    }
}
