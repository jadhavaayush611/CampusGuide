package com.campusguide.personal.ai.atlas.decision.evaluation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Composite score for a decision candidate breaking down evaluation factors.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionScore implements Serializable {

    private static final long serialVersionUID = 1L;

    private String candidateId;
    private double compositeScore;

    private double reasoningConfidence;
    private double evidenceQuality;
    private double policyComplianceScore;
    private double contextualRelevance;
    private double expectedUsefulness;
    private double userImpact;

    @Builder.Default
    private Map<String, Double> factorBreakdown = new ConcurrentHashMap<>();

    public static DecisionScore zero(String candidateId) {
        return DecisionScore.builder()
                .candidateId(candidateId)
                .compositeScore(0.0)
                .reasoningConfidence(0.0)
                .evidenceQuality(0.0)
                .policyComplianceScore(0.0)
                .contextualRelevance(0.0)
                .expectedUsefulness(0.0)
                .userImpact(0.0)
                .factorBreakdown(Map.of())
                .build();
    }
}
