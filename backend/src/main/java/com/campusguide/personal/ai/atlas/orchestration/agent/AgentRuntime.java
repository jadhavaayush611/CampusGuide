package com.campusguide.personal.ai.atlas.orchestration.agent;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.runtime.engine.ExecutionRuntime;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing agent runtime lifecycles, active agent instances, and delegated execution.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentRuntime {

    private final ExecutionRuntime executionRuntime;
    private final Map<String, AtlasAgent> activeAgents = new ConcurrentHashMap<>();

    public AtlasAgent registerAgent(AgentDescriptor descriptor) {
        if (descriptor == null || descriptor.getAgentId() == null) {
            throw new IllegalArgumentException("Descriptor and agentId must not be null");
        }

        AtlasAgent agent = new AtlasAgent(descriptor);
        agent.initialize();
        activeAgents.put(agent.getAgentId(), agent);
        log.info("AgentRuntime registered and initialized agent {}", agent.getAgentId());
        return agent;
    }

    public Optional<AtlasAgent> getAgent(String agentId) {
        return Optional.ofNullable(activeAgents.get(agentId));
    }

    public List<AtlasAgent> getAllAgents() {
        return new ArrayList<>(activeAgents.values());
    }

    public WorkflowInstance executeDelegatedTask(String agentId, ExecutionContext context, ExecutableWorkflow workflow) {
        AtlasAgent agent = activeAgents.get(agentId);
        if (agent == null) {
            throw new IllegalArgumentException("Agent not found with ID: " + agentId);
        }

        log.info("AgentRuntime delegating workflow {} to agent {}", workflow != null ? workflow.getWorkflowId() : "null", agentId);
        return agent.executeWorkflow(context, workflow, executionRuntime);
    }

    public void pauseAgent(String agentId) {
        AtlasAgent agent = activeAgents.get(agentId);
        if (agent != null) {
            agent.pause();
        }
    }

    public void resumeAgent(String agentId) {
        AtlasAgent agent = activeAgents.get(agentId);
        if (agent != null) {
            agent.resume();
        }
    }

    public void terminateAgent(String agentId) {
        AtlasAgent agent = activeAgents.remove(agentId);
        if (agent != null) {
            agent.terminate();
        }
    }
}
