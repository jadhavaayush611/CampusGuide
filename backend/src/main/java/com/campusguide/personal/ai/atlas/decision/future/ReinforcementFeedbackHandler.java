package com.campusguide.personal.ai.atlas.decision.future;

import com.campusguide.personal.ai.atlas.decision.model.DecisionOutcome;

/**
 * Extension point for receiving reinforcement feedback on decisions to update offline or online models.
 */
public interface ReinforcementFeedbackHandler {

    void recordFeedback(String decisionId, DecisionOutcome outcome, double reward, String feedbackChannel);
}
