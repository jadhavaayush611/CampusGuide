package com.campusguide.personal.ai.atlas.execution.rollback;

/**
 * Recovery strategy for execution failure rollback.
 */
public enum RecoveryStrategy {
    AUTOMATIC_COMPENSATING_ACTION,
    STATE_RESTORATION,
    MANUAL_INTERVENTION,
    IGNORE_AND_CONTINUE,
    BEST_EFFORT_CLEANUP
}
