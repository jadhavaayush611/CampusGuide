package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.orchestration.agent.AgentRuntime;
import com.campusguide.personal.ai.atlas.orchestration.agent.AtlasAgent;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentRegistry;
import com.campusguide.personal.ai.atlas.orchestration.supervisor.ExecutionAuditor;
import com.campusguide.personal.ai.atlas.orchestration.supervisor.PolicySupervisor;
import com.campusguide.personal.ai.atlas.orchestration.supervisor.RecoveryCoordinator;
import com.campusguide.personal.ai.atlas.orchestration.supervisor.SupervisorAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupervisorRuntimeTest {

    private AgentRegistry registry;
    private AgentRuntime runtime;
    private ExecutionAuditor auditor;
    private RecoveryCoordinator recoveryCoordinator;
    private PolicySupervisor policySupervisor;
    private SupervisorAgent supervisorAgent;

    @BeforeEach
    void setUp() {
        registry = new AgentRegistry();
        runtime = new AgentRuntime(null);
        auditor = new ExecutionAuditor(registry, runtime);
        recoveryCoordinator = new RecoveryCoordinator(registry, runtime);
        policySupervisor = new PolicySupervisor(registry);
        supervisorAgent = new SupervisorAgent();
    }

    @Test
    void testExecutionAuditorAndPolicyValidation() {
        AgentDescriptor descriptor = AgentDescriptor.builder()
                .agentId("agent_sub_1")
                .healthStatus(AgentDescriptor.HealthStatus.HEALTHY)
                .maxCapacity(5)
                .currentLoad(0)
                .build();

        registry.registerAgent(descriptor);
        AtlasAgent agent = runtime.registerAgent(descriptor);

        boolean validPolicy = policySupervisor.validatePolicy("agent_sub_1", 2);
        assertTrue(validPolicy);

        boolean invalidPolicy = policySupervisor.validatePolicy("agent_sub_1", 10);
        assertFalse(invalidPolicy);

        ExecutionAuditor.AuditReport report = auditor.auditExecution(5000);
        assertEquals(1, report.getTotalAgentsAudited());
        assertEquals(0, report.getStalledAgentIds().size());
    }

    @Test
    void testSupervisorAgentAndRecovery() {
        AgentDescriptor descriptor = AgentDescriptor.builder()
                .agentId("agent_sub_2")
                .healthStatus(AgentDescriptor.HealthStatus.HEALTHY)
                .build();

        registry.registerAgent(descriptor);
        runtime.registerAgent(descriptor);

        supervisorAgent.logSupervisionEvent("agent_sub_2", "STALL_CHECK", "Checking stall status");

        boolean recovered = recoveryCoordinator.recoverStalledAgent("agent_sub_2", "Inactivity timeout");
        assertTrue(recovered);
        assertEquals(AgentDescriptor.HealthStatus.DEGRADED, registry.getDescriptor("agent_sub_2").get().getHealthStatus());
    }
}
