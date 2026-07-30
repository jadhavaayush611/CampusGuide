package com.campusguide.personal.ai.atlas.decision.model;

import com.campusguide.personal.ai.atlas.decision.explanation.DecisionExplanation;
import com.campusguide.personal.ai.atlas.decision.metrics.DecisionMetrics;
import com.campusguide.personal.ai.atlas.decision.recommendation.RecommendationBundle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deterministic outcome produced by DecisionEngine.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionOutcome implements Serializable {

    private static final long serialVersionUID = 1L;

    private String outcomeId;
    private Decision decision;
    private DecisionStatus status;
    private DecisionCandidate selectedAction;

    @Builder.Default
    private Map<String, Object> executionHints = new ConcurrentHashMap<>();

    private RecommendationBundle recommendationBundle;
    private DecisionExplanation explanation;
    private DecisionMetrics metrics;

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static DecisionOutcome fallback(String outcomeId, String rationale) {
        DecisionCandidate fallbackCandidate = DecisionCandidate.simple(
                "cand_fallback",
                "FALLBACK_RESPONSE",
                "Fallback response due to low confidence or missing candidates",
                0.1
        );

        Decision decision = Decision.builder()
                .decisionId("dec_" + outcomeId)
                .selectedCandidate(fallbackCandidate)
                .confidence(0.1)
                .rationale(rationale)
                .candidates(Collections.singletonList(fallbackCandidate))
                .build();

        return DecisionOutcome.builder()
                .outcomeId(outcomeId)
                .decision(decision)
                .status(DecisionStatus.DEGRADED)
                .selectedAction(fallbackCandidate)
                .executionHints(Collections.singletonMap("isFallback", true))
                .timestamp(Instant.now())
                .build();
    }
}
