package com.campusguide.personal.ai.atlas.execution.runtime.statemachine;

import com.campusguide.personal.ai.atlas.execution.runtime.workflow.WorkflowState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates state transitions in the Execution Runtime Engine state machine.
 *
 * Supported state graph:
 * CREATED -> VALIDATED -> READY -> RUNNING -> WAITING / PAUSED -> COMPLETED
 * Recovery states:
 * FAILED, RETRYING, ROLLING_BACK, CANCELLED.
 */
@Component
public class TransitionValidator {

    private final Map<WorkflowState, Set<WorkflowState>> allowedTransitions = new HashMap<>();

    public TransitionValidator() {
        // CREATED -> VALIDATED, FAILED, CANCELLED
        allowedTransitions.put(WorkflowState.CREATED, EnumSet.of(WorkflowState.VALIDATED, WorkflowState.FAILED, WorkflowState.CANCELLED));

        // VALIDATED -> READY, FAILED, CANCELLED
        allowedTransitions.put(WorkflowState.VALIDATED, EnumSet.of(WorkflowState.READY, WorkflowState.FAILED, WorkflowState.CANCELLED));

        // READY -> RUNNING, PAUSED, CANCELLED, FAILED, ROLLING_BACK
        allowedTransitions.put(WorkflowState.READY, EnumSet.of(WorkflowState.RUNNING, WorkflowState.PAUSED, WorkflowState.CANCELLED, WorkflowState.FAILED, WorkflowState.ROLLING_BACK));

        // RUNNING -> WAITING, PAUSED, COMPLETED, FAILED, RETRYING, ROLLING_BACK, CANCELLED
        allowedTransitions.put(WorkflowState.RUNNING, EnumSet.of(
                WorkflowState.WAITING, WorkflowState.PAUSED, WorkflowState.COMPLETED,
                WorkflowState.FAILED, WorkflowState.RETRYING, WorkflowState.ROLLING_BACK, WorkflowState.CANCELLED
        ));

        // WAITING -> RUNNING, PAUSED, FAILED, CANCELLED, ROLLING_BACK
        allowedTransitions.put(WorkflowState.WAITING, EnumSet.of(WorkflowState.RUNNING, WorkflowState.PAUSED, WorkflowState.FAILED, WorkflowState.CANCELLED, WorkflowState.ROLLING_BACK));

        // PAUSED -> RUNNING, WAITING, CANCELLED, ROLLING_BACK
        allowedTransitions.put(WorkflowState.PAUSED, EnumSet.of(WorkflowState.RUNNING, WorkflowState.WAITING, WorkflowState.CANCELLED, WorkflowState.ROLLING_BACK));

        // RETRYING -> RUNNING, FAILED, ROLLING_BACK, CANCELLED
        allowedTransitions.put(WorkflowState.RETRYING, EnumSet.of(WorkflowState.RUNNING, WorkflowState.FAILED, WorkflowState.ROLLING_BACK, WorkflowState.CANCELLED));

        // ROLLING_BACK -> CANCELLED, FAILED
        allowedTransitions.put(WorkflowState.ROLLING_BACK, EnumSet.of(WorkflowState.CANCELLED, WorkflowState.FAILED));

        // Terminal states: COMPLETED, FAILED, CANCELLED (no outgoing transitions allowed by default except recovery retry on FAILED if allowed)
        allowedTransitions.put(WorkflowState.COMPLETED, EnumSet.noneOf(WorkflowState.class));
        allowedTransitions.put(WorkflowState.FAILED, EnumSet.of(WorkflowState.RETRYING, WorkflowState.ROLLING_BACK));
        allowedTransitions.put(WorkflowState.CANCELLED, EnumSet.noneOf(WorkflowState.class));
    }

    public boolean isValidTransition(WorkflowState current, WorkflowState target) {
        if (current == null || target == null) {
            return false;
        }
        if (current == target) {
            return true; // Self-transition is a no-op / allowed
        }
        Set<WorkflowState> validTargets = allowedTransitions.get(current);
        return validTargets != null && validTargets.contains(target);
    }

    public List<TransitionRule> getRulesForState(WorkflowState state) {
        List<TransitionRule> rules = new ArrayList<>();
        Set<WorkflowState> targets = allowedTransitions.get(state);
        if (targets != null) {
            for (WorkflowState target : targets) {
                rules.add(TransitionRule.builder()
                        .fromState(state)
                        .toState(target)
                        .description("Transition from " + state + " to " + target)
                        .build());
            }
        }
        return rules;
    }
}
