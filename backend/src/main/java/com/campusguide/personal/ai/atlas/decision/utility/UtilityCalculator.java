package com.campusguide.personal.ai.atlas.decision.utility;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.evaluation.DecisionScore;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility Engine calculator that combines factors into normalized utility scores.
 */
@Component
public class UtilityCalculator {

    private static final double WEIGHT_CONFIDENCE = 0.25;
    private static final double WEIGHT_RELEVANCE = 0.20;
    private static final double WEIGHT_URGENCY = 0.15;
    private static final double WEIGHT_IMPORTANCE = 0.15;
    private static final double WEIGHT_POLICY_ALIGNMENT = 0.15;
    private static final double WEIGHT_USER_BENEFIT = 0.10;

    public UtilityScore calculateUtility(DecisionCandidate candidate, DecisionContext context, DecisionScore decisionScore) {
        if (candidate == null) {
            return UtilityScore.zero("null_candidate");
        }

        double confidence = candidate.getConfidenceScore();
        double relevance = candidate.getFeasibilityScore();
        double urgency = 0.5;
        double importance = 0.5;
        double policyAlignment = decisionScore != null ? decisionScore.getPolicyComplianceScore() : 1.0;
        double userBenefit = candidate.getEstimatedUtility();

        if (context != null && context.getObjective() != null) {
            switch (context.getObjective().getPriority()) {
                case CRITICAL -> { urgency = 1.0; importance = 1.0; }
                case HIGH -> { urgency = 0.85; importance = 0.85; }
                case MEDIUM -> { urgency = 0.60; importance = 0.60; }
                case LOW -> { urgency = 0.30; importance = 0.30; }
            }
        }

        double rawUtility = (confidence * WEIGHT_CONFIDENCE)
                + (relevance * WEIGHT_RELEVANCE)
                + (urgency * WEIGHT_URGENCY)
                + (importance * WEIGHT_IMPORTANCE)
                + (policyAlignment * WEIGHT_POLICY_ALIGNMENT)
                + (userBenefit * WEIGHT_USER_BENEFIT);

        // Normalize between 0.0 and 1.0
        double normalized = Math.max(0.0, Math.min(1.0, rawUtility));

        Map<String, Double> factorScores = new HashMap<>();
        factorScores.put("confidence", confidence);
        factorScores.put("relevance", relevance);
        factorScores.put("urgency", urgency);
        factorScores.put("importance", importance);
        factorScores.put("policyAlignment", policyAlignment);
        factorScores.put("userBenefit", userBenefit);

        return UtilityScore.builder()
                .candidateId(candidate.getCandidateId())
                .totalUtility(rawUtility)
                .normalizedUtility(normalized)
                .factorScores(factorScores)
                .build();
    }
}
