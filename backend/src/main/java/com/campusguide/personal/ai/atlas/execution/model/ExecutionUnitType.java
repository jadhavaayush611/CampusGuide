package com.campusguide.personal.ai.atlas.execution.model;

/**
 * Type of execution unit within a workflow stage.
 */
public enum ExecutionUnitType {
    ACTION,
    QUERY,
    API_CALL,
    NOTIFICATION,
    MUTATION,
    COMPUTATION,
    VALIDATION
}
