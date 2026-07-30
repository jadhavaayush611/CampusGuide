package com.campusguide.personal.ai.atlas.decision.evaluation;

import com.campusguide.personal.ai.atlas.decision.context.DecisionContext;
import com.campusguide.personal.ai.atlas.decision.model.DecisionCandidate;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyComplianceResult;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard deterministic evaluation strategy combining confidence, evidence quality, policy compliance,
 * relevance, usefulness, and user impact into a composite score.
 */
@Component
public class DefaultEvaluationStrategy implements EvaluationStrategy {

    @Override
    public String getStrategyName() {
        return "DefaultEvaluationStrategy";
    }

    @Override
    public DecisionScore evaluateCandidate(DecisionCandidate candidate, DecisionContext context, PolicyComplianceResult policyCompliance) {
        if (candidate == null) {
            return DecisionScore.zero("null_candidate");
        }

        // 1. Reasoning confidence
        double confidence = candidate.getConfidenceScore();

        // 2. Evidence quality
        ReasoningEvidence evidence = context != null ? context.getReasoningEvidence() : null;
        double evidenceQuality = 0.50;
        if (evidence != null) {
            int nodeCount = evidence.getCitedNodeNames() != null ? evidence.getCitedNodeNames().size() : 0;
            evidenceQuality = Math.min(1.0, 0.40 + (nodeCount * 0.15));
        }

        // 3. Policy compliance
        double complianceScore = policyCompliance != null ? policyCompliance.getComplianceScore(candidate.getCandidateId()) : 1.0;

        // 4. Contextual relevance
        double relevance = candidate.getFeasibilityScore();

        // 5. Expected usefulness
        double usefulness = candidate.getEstimatedUtility();

        // 6. User impact (derived from priority or action type)
        double userImpact = 0.70;
        if (context != null && context.getObjective() != null) {
            switch (context.getObjective().getPriority()) {
                case CRITICAL -> userImpact = 1.0;
                case HIGH -> userImpact = 0.85;
                case MEDIUM -> userImpact = 0.70;
                case LOW -> userImpact = 0.50;
            }
        }

        // Weighted sum: confidence (0.25), evidence (0.20), compliance (0.20), relevance (0.15), usefulness (0.10), userImpact (0.10)
        double composite = (confidence * 0.25)
                + (evidenceQuality * 0.20)
                + (complianceScore * 0.20)
                + (relevance * 0.15)
                + (usefulness * 0.10)
                + (userImpact * 0.10);

        Map<String, Double> breakdown = new HashMap<>();
        breakdown.put("reasoningConfidence", confidence);
        breakdown.put("evidenceQuality", evidenceQuality);
        breakdown.put("policyCompliance", complianceScore);
        breakdown.put("contextualRelevance", relevance);
        breakdown.put("expectedUsefulness", usefulness);
        breakdown.put("userImpact", userImpact);

        return DecisionScore.builder()
                .candidateId(candidate.getCandidateId())
                .compositeScore(composite)
                .reasoningConfidence(confidence)
                .evidenceQuality(evidenceQuality)
                .policyComplianceScore(complianceScore)
                .contextualRelevance(relevance)
                .expectedUsefulness(usefulness)
                .userImpact(userImpact)
                .factorBreakdown(breakdown)
                .build();
    }
}
