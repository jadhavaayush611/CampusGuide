package com.campusguide.personal.ai.atlas.execution.runtime.workflow;

/**
 * Deterministic state enum representing the execution state of a workflow instance.
 */
public enum WorkflowState {
    CREATED,
    VALIDATED,
    READY,
    RUNNING,
    WAITING,
    PAUSED,
    COMPLETED,
    FAILED,
    RETRYING,
    ROLLING_BACK,
    CANCELLED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }

    public boolean isRecovery() {
        return this == RETRYING || this == ROLLING_BACK;
    }

    public boolean isPausedOrWaiting() {
        return this == PAUSED || this == WAITING;
    }

    public boolean isRunning() {
        return this == RUNNING || this == RETRYING || this == ROLLING_BACK;
    }
}
