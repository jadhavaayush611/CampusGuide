package com.campusguide.personal.ai.atlas.execution.runtime.tool;

/**
 * Status of a tool invocation.
 */
public enum ToolExecutionStatus {
    SUCCESS,
    FAILURE,
    WAITING_FOR_APPROVAL,
    TIMED_OUT,
    DENIED,
    CANCELLED;

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
