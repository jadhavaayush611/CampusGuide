package com.campusguide.personal.ai.atlas.decision.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Diagnostic metrics capturing Decision Engine performance, candidate counts,
 * policy evaluations, rejected candidates, utility & confidence distributions,
 * without storing any sensitive user or text data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionMetrics implements Serializable {

    private static final long serialVersionUID = 1L;

    private long decisionLatencyMs;
    private int totalCandidatesGenerated;
    private int totalPoliciesEvaluated;
    private int rejectedCandidateCount;

    private double selectedCandidateUtility;
    private double selectedCandidateConfidence;

    @Builder.Default
    private Map<String, Integer> candidatesByActionType = new ConcurrentHashMap<>();

    @Builder.Default
    private Map<String, Integer> rejectionsByPolicy = new ConcurrentHashMap<>();

    public static DecisionMetrics createEmpty() {
        return DecisionMetrics.builder()
                .decisionLatencyMs(0)
                .totalCandidatesGenerated(0)
                .totalPoliciesEvaluated(0)
                .rejectedCandidateCount(0)
                .selectedCandidateUtility(0.0)
                .selectedCandidateConfidence(0.0)
                .candidatesByActionType(new ConcurrentHashMap<>())
                .rejectionsByPolicy(new ConcurrentHashMap<>())
                .build();
    }
}
