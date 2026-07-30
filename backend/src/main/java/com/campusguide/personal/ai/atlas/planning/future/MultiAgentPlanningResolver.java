package com.campusguide.personal.ai.atlas.planning.future;

import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;

import java.util.List;

/**
 * SPI Extension interface for multi-agent plan coordination.
 */
public interface MultiAgentPlanningResolver {

    ExecutionPlan coordinateAgentPlans(List<ExecutionPlan> agentPlans);
}
