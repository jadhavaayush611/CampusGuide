package com.campusguide.personal.ai.atlas.decision.future;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;

import java.util.List;

/**
 * Extension point for multi-agent or consensus collaborative decision resolution.
 */
public interface CollaborativeDecisionResolver {

    DecisionCandidate resolveConsensus(List<DecisionCandidate> candidates, DecisionContext context);
}
