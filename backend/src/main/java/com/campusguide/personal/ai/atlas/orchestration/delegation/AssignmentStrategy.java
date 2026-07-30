package com.campusguide.personal.ai.atlas.orchestration.delegation;

/**
 * Strategy for selecting optimal agent for task delegation.
 */
public enum AssignmentStrategy {
    CAPABILITY_BASED,
    LOAD_BALANCED,
    PRIORITY_AWARE,
    LOCALITY_AWARE,
    HYBRID
}
