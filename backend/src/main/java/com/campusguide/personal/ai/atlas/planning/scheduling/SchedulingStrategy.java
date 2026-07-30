package com.campusguide.personal.ai.atlas.planning.scheduling;

import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;

/**
 * Strategy SPI interface for task scheduling algorithms.
 */
public interface SchedulingStrategy {

    String getStrategyName();

    Schedule schedule(TaskGraph taskGraph, PlanningContext context);
}
