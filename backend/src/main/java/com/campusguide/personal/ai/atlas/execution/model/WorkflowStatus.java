package com.campusguide.personal.ai.atlas.execution.model;

/**
 * Status of an ExecutableWorkflow during preparation lifecycle.
 */
public enum WorkflowStatus {
    DRAFT,
    PREPARED,
    VALIDATED,
    APPROVAL_REQUIRED,
    APPROVED,
    REJECTED,
    READY,
    DEGRADED
}
