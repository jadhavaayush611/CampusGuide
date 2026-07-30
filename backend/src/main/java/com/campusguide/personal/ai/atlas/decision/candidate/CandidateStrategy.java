package com.campusguide.personal.ai.atlas.decision.candidate;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;

import java.util.List;

/**
 * Strategy interface for pluggable decision candidate generation.
 */
public interface CandidateStrategy {

    /**
     * Unique identifier for this candidate strategy.
     */
    String getStrategyId();

    /**
     * Priority order for candidate strategy execution. Lower values denote higher priority.
     */
    default int getOrder() {
        return 100;
    }

    /**
     * Evaluates whether this strategy supports generating candidates for the given context.
     */
    boolean supports(DecisionContext context);

    /**
     * Generates a list of decision candidates for the given context.
     */
    List<DecisionCandidate> generateCandidates(DecisionContext context);
}
