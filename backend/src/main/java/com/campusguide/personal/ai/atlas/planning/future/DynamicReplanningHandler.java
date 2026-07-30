package com.campusguide.personal.ai.atlas.planning.future;

import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;

/**
 * SPI Extension interface for dynamic runtime replanning.
 */
public interface DynamicReplanningHandler {

    ExecutionPlan replan(ExecutionPlan originalPlan, String failedTaskId, String failureReason);
}
