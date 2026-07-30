package com.campusguide.personal.ai.atlas.decision.future;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;

import java.util.List;
import java.util.Map;

/**
 * Extension point for user personalization and preference scoring adjustment.
 */
public interface PersonalizationProvider {

    Map<String, Double> calculatePersonalizationWeights(String userId, DecisionContext context, List<DecisionCandidate> candidates);
}
