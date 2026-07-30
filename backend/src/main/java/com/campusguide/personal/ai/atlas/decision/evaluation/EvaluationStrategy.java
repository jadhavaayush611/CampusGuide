package com.campusguide.personal.ai.atlas.decision.evaluation;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyComplianceResult;

/**
 * Strategy for deterministic candidate decision scoring.
 */
public interface EvaluationStrategy {

    String getStrategyName();

    DecisionScore evaluateCandidate(DecisionCandidate candidate, DecisionContext context, PolicyComplianceResult policyCompliance);
}
