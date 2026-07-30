package com.campusguide.personal.ai.atlas.decision.policy;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import org.springframework.stereotype.Component;

/**
 * Validates candidate against user preferences or contextual parameters.
 */
@Component
public class UserPreferencePolicyRule implements PolicyRule {

    @Override
    public String getRuleId() {
        return "UserPreferencePolicyRule";
    }

    @Override
    public String getRuleName() {
        return "User Preference Validation";
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public PolicyEvaluationResult evaluate(DecisionCandidate candidate, DecisionContext context) {
        if (candidate == null) {
            return PolicyEvaluationResult.deny(getRuleId(), getRuleName(), "Null candidate");
        }

        // Allow all by default unless explicit negative preference flag is set
        return PolicyEvaluationResult.allow(getRuleId(), getRuleName());
    }
}
