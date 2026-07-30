package com.campusguide.personal.ai.atlas.planning.model;

/**
 * Status of an ExecutionPlan throughout its lifecycle.
 */
public enum PlanStatus {
    DRAFT,
    VALIDATED,
    OPTIMIZED,
    READY,
    FAILED,
    DEGRADED
}
