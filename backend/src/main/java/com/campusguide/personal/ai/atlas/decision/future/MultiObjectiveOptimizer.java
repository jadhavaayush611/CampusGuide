package com.campusguide.personal.ai.atlas.decision.future;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;

import java.util.List;

/**
 * Extension point for multi-objective optimization (Pareto frontier selection).
 */
public interface MultiObjectiveOptimizer {

    List<DecisionCandidate> optimizeParetoFrontier(List<DecisionCandidate> candidates, DecisionContext context);
}
