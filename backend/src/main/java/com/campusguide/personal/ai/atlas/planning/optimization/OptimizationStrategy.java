package com.campusguide.personal.ai.atlas.planning.optimization;

import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;

/**
 * SPI Strategy interface for plan optimization algorithms.
 */
public interface OptimizationStrategy {

    String getStrategyName();

    OptimizationResult optimize(ExecutionPlan plan, TaskGraph graph, PlanningContext context);
}
