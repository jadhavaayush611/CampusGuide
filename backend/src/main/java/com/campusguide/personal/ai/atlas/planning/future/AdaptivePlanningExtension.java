package com.campusguide.personal.ai.atlas.planning.future;

import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;

import java.util.Map;

/**
 * SPI Extension interface for adaptive planning capabilities.
 */
public interface AdaptivePlanningExtension {

    ExecutionPlan adaptPlan(ExecutionPlan currentPlan, Map<String, Object> environmentDelta);
}
