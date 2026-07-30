package com.campusguide.personal.ai.atlas.decision.future;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.policy.DecisionPolicy;

import java.util.List;

/**
 * Extension point for adaptive policy engines that adjust policies based on environment or historical outcomes.
 */
public interface AdaptivePolicyEngine {

    List<DecisionPolicy> adaptPolicies(DecisionContext context, List<DecisionPolicy> currentPolicies);
}
