package com.campusguide.personal.ai.atlas.execution.runtime.recovery;

/**
 * Options for workflow failure recovery.
 */
public enum RecoveryPolicy {
    RETRY_UNIT,
    RESTORE_CHECKPOINT,
    ROLLBACK_WORKFLOW,
    WAIT_FOR_HUMAN,
    CANCEL
}
