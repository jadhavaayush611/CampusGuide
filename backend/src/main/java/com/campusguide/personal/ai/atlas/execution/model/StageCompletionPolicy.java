package com.campusguide.personal.ai.atlas.execution.model;

/**
 * Policy defining completion conditions for execution units within a stage.
 */
public enum StageCompletionPolicy {
    ALL_MUST_SUCCEED,
    ANY_MUST_SUCCEED,
    AT_LEAST_ONE,
    MAJORITY_MUST_SUCCEED
}
