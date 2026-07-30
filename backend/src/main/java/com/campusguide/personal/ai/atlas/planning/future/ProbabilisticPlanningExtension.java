package com.campusguide.personal.ai.atlas.planning.future;

import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;

import java.util.Map;

/**
 * SPI Extension interface for probabilistic planning and risk evaluation.
 */
public interface ProbabilisticPlanningExtension {

    ExecutionPlan evaluateProbabilisticPlan(ExecutionPlan plan, Map<String, Double> riskFactors);
}
