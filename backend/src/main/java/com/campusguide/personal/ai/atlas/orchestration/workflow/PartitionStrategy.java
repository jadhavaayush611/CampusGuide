package com.campusguide.personal.ai.atlas.orchestration.workflow;

/**
 * Strategy for partitioning an ExecutableWorkflow into parallel/sequential sub-workflows.
 */
public enum PartitionStrategy {
    STAGE_BASED,
    CAPABILITY_BASED,
    DATA_LOCALITY,
    BALANCED
}
