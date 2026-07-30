package com.campusguide.personal.ai.atlas.execution.runtime.statemachine;

import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowInstance;
import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Manages runtime state transitions for WorkflowInstances following deterministic transition rules.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionStateMachine {

    private final TransitionValidator transitionValidator;

    public synchronized boolean transition(WorkflowInstance instance, WorkflowState targetState, String reason) {
        if (instance == null || targetState == null) {
            log.warn("Null instance or target state provided for state machine transition");
            return false;
        }

        WorkflowState currentState = instance.getState();
        if (currentState == targetState) {
            return true;
        }

        if (!transitionValidator.isValidTransition(currentState, targetState)) {
            log.error("Invalid workflow state transition attempted for instance {}: {} -> {}",
                    instance.getInstanceId(), currentState, targetState);
            throw new IllegalStateException("Invalid state transition from " + currentState + " to " + targetState);
        }

        log.info("Transitioning workflow instance {} from {} to {} (Reason: {})",
                instance.getInstanceId(), currentState, targetState, reason);

        instance.setState(targetState);
        instance.addLog("State changed: " + currentState + " -> " + targetState + " | Reason: " + (reason != null ? reason : "N/A"));

        if (targetState.isTerminal()) {
            instance.setEndTime(Instant.now());
        }

        return true;
    }
}
