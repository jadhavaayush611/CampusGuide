package com.campusguide.personal.ai.atlas.planning.graph;

/**
 * State of a PlanningTask in the task graph or execution plan.
 */
public enum TaskState {
    PENDING,
    READY,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED,
    FAILED,
    CANCELLED
}
