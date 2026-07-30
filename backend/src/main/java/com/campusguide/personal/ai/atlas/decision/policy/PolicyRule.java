package com.campusguide.personal.ai.atlas.decision.policy;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;

/**
 * Interface defining an individual policy rule.
 */
public interface PolicyRule {

    String getRuleId();

    String getRuleName();

    default int getPriority() {
        return 100;
    }

    PolicyEvaluationResult evaluate(DecisionCandidate candidate, DecisionContext context);
}
