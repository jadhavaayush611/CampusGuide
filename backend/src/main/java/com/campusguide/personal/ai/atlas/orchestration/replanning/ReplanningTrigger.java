package com.campusguide.personal.ai.atlas.orchestration.replanning;

/**
 * Triggers indicating the need for dynamic workflow replanning.
 */
public enum ReplanningTrigger {
    EXECUTION_FAILURE,
    UNAVAILABLE_CAPABILITY,
    ENVIRONMENTAL_CHANGE,
    USER_CONSTRAINT_CHANGE,
    POLICY_VIOLATION
}
