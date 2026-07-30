package com.campusguide.personal.ai.atlas.orchestration.agent;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.runtime.engine.ExecutionRuntime;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import com.campusguide.personal.ai.atlas.orchestration.registry.AgentDescriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Provider-independent specialized agent in Atlas Multi-Agent Orchestration Layer.
 * Delegates workflow execution directly to the existing Execution Runtime without duplicating execution logic.
 */
@Slf4j
public class AtlasAgent {

    @Getter
    private final String agentId;
    @Getter
    private final String name;
    @Getter
    private final AgentDescriptor descriptor;

    @Getter
    private final AgentState state;

    @Getter
    @Setter
    private AgentSession currentSession;

    public AtlasAgent(AgentDescriptor descriptor) {
        if (descriptor == null || descriptor.getAgentId() == null) {
            throw new IllegalArgumentException("AgentDescriptor and agentId must not be null");
        }
        this.agentId = descriptor.getAgentId();
        this.name = descriptor.getName() != null ? descriptor.getName() : descriptor.getAgentId();
        this.descriptor = descriptor;
        this.state = AgentState.ready(this.agentId);
        this.state.setLifecycle(AgentLifecycle.READY);
    }

    /**
     * Initializes the agent lifecycle.
     */
    public synchronized void initialize() {
        if (state.getLifecycle() == AgentLifecycle.UNINITIALIZED || state.getLifecycle() == AgentLifecycle.STOPPED) {
            log.info("Initializing AtlasAgent {}", agentId);
            state.setLifecycle(AgentLifecycle.INITIALIZING);
            // Setup agent runtime state
            state.setLifecycle(AgentLifecycle.READY);
            state.updateActivity();
        }
    }

    /**
     * Executes an assigned workflow task by delegating to the ExecutionRuntime.
     */
    public WorkflowInstance executeWorkflow(ExecutionContext context, ExecutableWorkflow workflow, ExecutionRuntime executionRuntime) {
        if (state.getLifecycle() == AgentLifecycle.TERMINATED || state.getLifecycle() == AgentLifecycle.FAILED) {
            log.warn("Agent {} is in state {} and cannot execute workflow", agentId, state.getLifecycle());
            return null;
        }

        if (executionRuntime == null) {
            throw new IllegalArgumentException("ExecutionRuntime must not be null for agent delegated execution");
        }

        String taskId = workflow != null ? workflow.getWorkflowId() : "task_" + UUID.randomUUID().toString().substring(0, 8);
        String workflowId = workflow != null ? workflow.getWorkflowId() : "wf_" + UUID.randomUUID().toString().substring(0, 8);

        AgentSession session = AgentSession.start(agentId, taskId, workflowId);
        this.currentSession = session;

        synchronized (this) {
            state.setLifecycle(AgentLifecycle.RUNNING);
            state.setCurrentTaskId(taskId);
            state.setCurrentWorkflowId(workflowId);
            state.incrementLoad();
            descriptor.setCurrentLoad(state.getActiveLoad());
        }

        log.info("Agent {} executing delegated workflow {} via ExecutionRuntime", agentId, workflowId);

        try {
            WorkflowInstance resultInstance = executionRuntime.executeWorkflow(context, workflow);
            
            synchronized (this) {
                state.decrementLoad();
                descriptor.setCurrentLoad(state.getActiveLoad());
                if (resultInstance != null && resultInstance.getState() == WorkflowState.COMPLETED) {
                    session.complete();
                    state.setLifecycle(AgentLifecycle.READY);
                    log.info("Agent {} successfully completed workflow {}", agentId, workflowId);
                } else if (resultInstance != null && resultInstance.getState() == WorkflowState.FAILED) {
                    session.fail("Workflow execution failed in ExecutionRuntime");
                    state.setLifecycle(AgentLifecycle.READY);
                    log.warn("Agent {} workflow execution failed for workflow {}", agentId, workflowId);
                } else {
                    state.setLifecycle(AgentLifecycle.READY);
                }
                state.setCurrentTaskId(null);
                state.setCurrentWorkflowId(null);
            }
            return resultInstance;
        } catch (Exception e) {
            log.error("Error executing workflow in agent {}: {}", agentId, e.getMessage(), e);
            synchronized (this) {
                state.decrementLoad();
                descriptor.setCurrentLoad(state.getActiveLoad());
                state.setErrorMessage(e.getMessage());
                session.fail(e.getMessage());
                state.setLifecycle(AgentLifecycle.FAILED);
                state.setCurrentTaskId(null);
                state.setCurrentWorkflowId(null);
            }
            throw e;
        }
    }

    public synchronized void pause() {
        if (state.getLifecycle() == AgentLifecycle.RUNNING || state.getLifecycle() == AgentLifecycle.READY) {
            state.setLifecycle(AgentLifecycle.PAUSED);
            log.info("Agent {} paused", agentId);
        }
    }

    public synchronized void resume() {
        if (state.getLifecycle() == AgentLifecycle.PAUSED) {
            state.setLifecycle(AgentLifecycle.READY);
            log.info("Agent {} resumed", agentId);
        }
    }

    public synchronized void terminate() {
        state.setLifecycle(AgentLifecycle.TERMINATED);
        state.setActiveLoad(0);
        descriptor.setCurrentLoad(0);
        log.info("Agent {} terminated", agentId);
    }
}
