package com.campusguide.personal.ai.atlas.decision.policy;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Enforces safety boundaries and restricted action types.
 */
@Component
public class SafetyPolicyRule implements PolicyRule {

    @Override
    public String getRuleId() {
        return "SafetyPolicyRule";
    }

    @Override
    public String getRuleName() {
        return "Safety & Boundary Validation";
    }

    @Override
    public int getPriority() {
        return 5; // Highest priority
    }

    @Override
    public PolicyEvaluationResult evaluate(DecisionCandidate candidate, DecisionContext context) {
        if (candidate == null) {
            return PolicyEvaluationResult.deny(getRuleId(), getRuleName(), "Null candidate");
        }

        Set<String> restrictedActionTypes = context != null && context.getScope() != null 
                ? context.getScope().getRestrictedActionTypes() : Set.of();

        if (restrictedActionTypes.contains(candidate.getActionType())) {
            return PolicyEvaluationResult.deny(
                    getRuleId(),
                    getRuleName(),
                    "Action type " + candidate.getActionType() + " is in restricted scope"
            );
        }

        return PolicyEvaluationResult.allow(getRuleId(), getRuleName());
    }
}
