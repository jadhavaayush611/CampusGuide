package com.campusguide.personal.ai.atlas.decision.policy;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Validates candidate permissions against DecisionContext permissions and required permissions.
 */
@Component
public class PermissionPolicyRule implements PolicyRule {

    @Override
    public String getRuleId() {
        return "PermissionPolicyRule";
    }

    @Override
    public String getRuleName() {
        return "Permission Policy Validation";
    }

    @Override
    public int getPriority() {
        return 10; // High priority validation
    }

    @Override
    public PolicyEvaluationResult evaluate(DecisionCandidate candidate, DecisionContext context) {
        if (candidate == null) {
            return PolicyEvaluationResult.deny(getRuleId(), getRuleName(), "Null candidate");
        }

        Set<String> requiredPermissions = context != null && context.getConstraints() != null 
                ? context.getConstraints().getRequiredPermissions() : null;

        if (requiredPermissions != null && !requiredPermissions.isEmpty()) {
            Set<String> userPermissions = context != null && context.getPermissions() != null 
                    ? context.getPermissions() : Set.of();
            if (!userPermissions.containsAll(requiredPermissions)) {
                return PolicyEvaluationResult.deny(
                        getRuleId(), 
                        getRuleName(), 
                        "User lacks required permissions: " + requiredPermissions
                );
            }
        }

        return PolicyEvaluationResult.allow(getRuleId(), getRuleName());
    }
}
