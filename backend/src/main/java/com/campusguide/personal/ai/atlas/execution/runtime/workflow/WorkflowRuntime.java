package com.campusguide.personal.ai.atlas.execution.runtime.workflow;

import com.campusguide.personal.ai.atlas.execution.context.ExecutionContext;
import com.campusguide.personal.ai.atlas.execution.model.ExecutableWorkflow;
import com.campusguide.personal.ai.atlas.execution.runtime.engine.ExecutionCoordinator;
import com.campusguide.personal.ai.atlas.execution.runtime.engine.ExecutionRuntime;
import com.campusguide.personal.ai.atlas.execution.runtime.events.EventPublisher;
import com.campusguide.personal.ai.atlas.execution.runtime.events.WorkflowEvent;
import com.campusguide.personal.ai.atlas.execution.runtime.human.ExecutionControlService;
import com.campusguide.personal.ai.atlas.execution.runtime.rollback.RollbackExecutor;
import com.campusguide.personal.ai.atlas.execution.runtime.statemachine.ExecutionStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Main facade and service orchestrating Atlas Workflow Runtime Engine.
 * Manages workflow instantiation, execution lifecycle, runtime state, persistence, and APIs.
 *
 * STRICT INVARIANT:
 * Consumes ONLY ExecutionContext and ExecutableWorkflow.
 * Never inspects ReasoningContext, DecisionContext, PlanningContext, or ExecutionPlan directly.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowRuntime implements ExecutionRuntime {

    private final ExecutionCoordinator executionCoordinator;
    private final ExecutionStateMachine stateMachine;
    private final RollbackExecutor rollbackExecutor;
    private final ExecutionControlService controlService;
    private final EventPublisher eventPublisher;

    private final Map<String, WorkflowInstance> activeInstances = new ConcurrentHashMap<>();

    public WorkflowInstance createInstance(ExecutionContext context, ExecutableWorkflow workflow) {
        if (workflow == null) {
            log.warn("Cannot create WorkflowInstance for null ExecutableWorkflow");
            return null;
        }

        WorkflowInstance instance = WorkflowInstance.create(context, workflow);
        activeInstances.put(instance.getInstanceId(), instance);

        stateMachine.transition(instance, WorkflowState.VALIDATED, "Workflow validated");
        stateMachine.transition(instance, WorkflowState.READY, "Workflow ready for execution");

        eventPublisher.publishWorkflowEvent(WorkflowEvent.builder()
                .workflowId(instance.getWorkflowId())
                .instanceId(instance.getInstanceId())
                .eventType("WORKFLOW_CREATED")
                .previousState(WorkflowState.CREATED)
                .newState(WorkflowState.READY)
                .message("Workflow instance created and ready")
                .build());

        log.info("Created WorkflowInstance {} for workflowId {}", instance.getInstanceId(), instance.getWorkflowId());
        return instance;
    }

    @Override
    public WorkflowInstance executeWorkflow(ExecutionContext context, ExecutableWorkflow workflow) {
        WorkflowInstance instance = createInstance(context, workflow);
        if (instance == null) {
            return null;
        }
        executeInstance(instance.getInstanceId());
        return instance;
    }

    public WorkflowInstance executeInstance(String instanceId) {
        WorkflowInstance instance = activeInstances.get(instanceId);
        if (instance == null) {
            log.error("WorkflowInstance {} not found for execution", instanceId);
            return null;
        }

        eventPublisher.publishWorkflowEvent(WorkflowEvent.builder()
                .workflowId(instance.getWorkflowId())
                .instanceId(instance.getInstanceId())
                .eventType("WORKFLOW_STARTED")
                .previousState(instance.getState())
                .newState(WorkflowState.RUNNING)
                .message("Workflow execution started")
                .build());

        boolean success = executionCoordinator.executeWorkflow(instance);

        WorkflowState finalState = instance.getState();
        eventPublisher.publishWorkflowEvent(WorkflowEvent.builder()
                .workflowId(instance.getWorkflowId())
                .instanceId(instance.getInstanceId())
                .eventType(success ? "WORKFLOW_COMPLETED" : "WORKFLOW_FAILED")
                .newState(finalState)
                .message("Workflow execution finished with state: " + finalState)
                .build());

        return instance;
    }

    public WorkflowInstance getInstance(String instanceId) {
        return activeInstances.get(instanceId);
    }

    public List<WorkflowInstance> getActiveInstances() {
        return new ArrayList<>(activeInstances.values());
    }

    public List<WorkflowInstance> getRunningInstances() {
        return activeInstances.values().stream()
                .filter(i -> i.getState().isRunning())
                .collect(Collectors.toList());
    }

    public void pauseWorkflow(String instanceId, String reason) {
        WorkflowInstance instance = activeInstances.get(instanceId);
        if (instance != null) {
            stateMachine.transition(instance, WorkflowState.PAUSED, reason);
            controlService.recordIntervention(instance.getWorkflowId(), instanceId, null, "PAUSE", "system", "PAUSED", reason);
            eventPublisher.publishWorkflowEvent(WorkflowEvent.builder()
                    .workflowId(instance.getWorkflowId())
                    .instanceId(instanceId)
                    .eventType("WORKFLOW_PAUSED")
                    .newState(WorkflowState.PAUSED)
                    .message("Workflow paused: " + reason)
                    .build());
        }
    }

    public void resumeWorkflow(String instanceId) {
        WorkflowInstance instance = activeInstances.get(instanceId);
        if (instance != null && instance.getState().isPausedOrWaiting()) {
            stateMachine.transition(instance, WorkflowState.RUNNING, "Resuming workflow execution");
            controlService.recordIntervention(instance.getWorkflowId(), instanceId, null, "RESUME", "system", "RESUMED", "Workflow resumed");
            eventPublisher.publishWorkflowEvent(WorkflowEvent.builder()
                    .workflowId(instance.getWorkflowId())
                    .instanceId(instanceId)
                    .eventType("WORKFLOW_RESUMED")
                    .newState(WorkflowState.RUNNING)
                    .message("Workflow resumed")
                    .build());
            executeInstance(instanceId);
        }
    }

    public void cancelWorkflow(String instanceId, String reason) {
        WorkflowInstance instance = activeInstances.get(instanceId);
        if (instance != null && !instance.getState().isTerminal()) {
            log.warn("Cancelling workflow instance {} (Reason: {})", instanceId, reason);
            rollbackExecutor.rollbackWorkflow(instance, "Workflow cancelled: " + reason);
            controlService.recordIntervention(instance.getWorkflowId(), instanceId, null, "CANCEL", "system", "CANCELLED", reason);
            eventPublisher.publishWorkflowEvent(WorkflowEvent.builder()
                    .workflowId(instance.getWorkflowId())
                    .instanceId(instanceId)
                    .eventType("WORKFLOW_CANCELLED")
                    .newState(WorkflowState.CANCELLED)
                    .message("Workflow cancelled: " + reason)
                    .build());
        }
    }
}
