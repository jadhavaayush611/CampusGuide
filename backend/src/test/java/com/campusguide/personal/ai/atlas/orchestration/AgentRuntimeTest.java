package com.campusguide.personal.ai.atlas.orchestration;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.runtime.engine.ExecutionRuntime;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import com.campusguide.personal.ai.atlas.orchestration.agent.AgentLifecycle;
import com.campusguide.personal.ai.atlas.orchestration.agent.AgentRuntime;
import com.campusguide.personal.ai.atlas.orchestration.agent.AtlasAgent;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentCapability;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentRuntimeTest {

    @Mock
    private ExecutionRuntime executionRuntime;

    private AgentRuntime agentRuntime;
    private AgentDescriptor testDescriptor;

    @BeforeEach
    void setUp() {
        agentRuntime = new AgentRuntime(executionRuntime);
        testDescriptor = AgentDescriptor.builder()
                .agentId("agent_academic_1")
                .name("Academic Agent")
                .agentType("ACADEMIC_SPECIALIST")
                .version("1.0.0")
                .healthStatus(AgentDescriptor.HealthStatus.HEALTHY)
                .maxCapacity(5)
                .currentLoad(0)
                .capabilities(List.of(AgentCapability.of("CourseQuery", "academic")))
                .metadata(AgentMetadata.defaultFor("Academic Querying", "academic"))
                .build();
    }

    @Test
    void testRegisterAndRetrieveAgent() {
        AtlasAgent agent = agentRuntime.registerAgent(testDescriptor);

        assertNotNull(agent);
        assertEquals("agent_academic_1", agent.getAgentId());
        assertEquals(AgentLifecycle.READY, agent.getState().getLifecycle());
        assertTrue(agentRuntime.getAgent("agent_academic_1").isPresent());
    }

    @Test
    void testExecuteDelegatedTask() {
        AtlasAgent agent = agentRuntime.registerAgent(testDescriptor);

        ExecutionContext context = ExecutionContext.builder().contextId("ctx_1").build();
        ExecutableWorkflow workflow = ExecutableWorkflow.builder().workflowId("wf_1").build();
        WorkflowInstance instance = WorkflowInstance.builder().workflowId("wf_1").state(WorkflowState.COMPLETED).build();

        when(executionRuntime.executeWorkflow(any(), any())).thenReturn(instance);

        WorkflowInstance result = agentRuntime.executeDelegatedTask("agent_academic_1", context, workflow);

        assertNotNull(result);
        assertEquals(WorkflowState.COMPLETED, result.getState());
        assertEquals(AgentLifecycle.READY, agent.getState().getLifecycle());
        assertEquals(0, agent.getState().getActiveLoad());
    }

    @Test
    void testPauseResumeTerminateLifecycle() {
        AtlasAgent agent = agentRuntime.registerAgent(testDescriptor);

        agentRuntime.pauseAgent("agent_academic_1");
        assertEquals(AgentLifecycle.PAUSED, agent.getState().getLifecycle());

        agentRuntime.resumeAgent("agent_academic_1");
        assertEquals(AgentLifecycle.READY, agent.getState().getLifecycle());

        agentRuntime.terminateAgent("agent_academic_1");
        assertTrue(agentRuntime.getAgent("agent_academic_1").isEmpty());
    }
}
