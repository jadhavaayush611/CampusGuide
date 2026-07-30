package com.campusguide.personal.ai.atlas.execution.runtime.rollback;

import com.campusguide.personal.ai.atlas.execution.model.ExecutionUnit;
import com.campusguide.personal.ai.atlas.execution.rollback.RollbackPlan;
import com.campusguide.personal.ai.atlas.execution.runtime.checkpoint.CheckpointManager;
import com.campusguide.personal.ai.atlas.execution.runtime.events.EventPublisher;
import com.campusguide.personal.ai.atlas.execution.runtime.events.WorkflowEvent;
import com.campusguide.personal.ai.atlas.execution.runtime.statemachine.ExecutionStateMachine;
import com.campusguide.personal.ai.atlas.execution.runtime.tool.ToolResult;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Coordinates rollback execution process, transitioning state to ROLLING_BACK,
 * executing compensating actions, restoring checkpoints, and managing rollback lifecycle.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RollbackCoordinator {

    private final CompensationExecutor compensationExecutor;
    private final ExecutionStateMachine stateMachine;
    private final CheckpointManager checkpointManager;
    private final EventPublisher eventPublisher;

    public boolean executeRollback(WorkflowInstance instance, RollbackPlan rollbackPlan, String reason) {
        if (instance == null || rollbackPlan == null) {
            log.warn("Null instance or rollback plan provided for rollback execution");
            return false;
        }

        log.info("Initiating rollback for workflow instance {} (Reason: {})", instance.getInstanceId(), reason);
        stateMachine.transition(instance, WorkflowState.ROLLING_BACK, reason);

        eventPublisher.publishWorkflowEvent(WorkflowEvent.builder()
                .workflowId(instance.getWorkflowId())
                .instanceId(instance.getInstanceId())
                .eventType("WORKFLOW_ROLLBACK")
                .previousState(instance.getState())
                .newState(WorkflowState.ROLLING_BACK)
                .message("Rollback initiated: " + reason)
                .build());

        List<ExecutionUnit> rollbackUnits = new ArrayList<>(rollbackPlan.getRollbackUnits());
        // Execute compensating actions in reverse deterministic order
        Collections.reverse(rollbackUnits);

        boolean allCompensated = true;
        for (ExecutionUnit unit : rollbackUnits) {
            ToolResult res = compensationExecutor.executeCompensatingUnit(instance.getContext(), unit, instance.getWorkflowId());
            if (res == null || !res.getStatus().isSuccess()) {
                log.error("Failed to execute compensating unit {} during rollback of workflow instance {}",
                        unit.getUnitId(), instance.getInstanceId());
                allCompensated = false;
            }
        }

        // Restore latest checkpoint if available
        checkpointManager.getLatestCheckpoint(instance.getWorkflowId()).ifPresent(chk -> {
            checkpointManager.restoreFromCheckpoint(instance, chk.getCheckpointId());
        });

        // Transition to final state: CANCELLED if successful rollback, FAILED if compensating actions failed
        WorkflowState finalState = allCompensated ? WorkflowState.CANCELLED : WorkflowState.FAILED;
        stateMachine.transition(instance, finalState, "Rollback completed " + (allCompensated ? "successfully" : "with failures"));
        return allCompensated;
    }
}
