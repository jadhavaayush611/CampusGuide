package com.campusguide.personal.ai.atlas.decision.explanation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detailed explanation object answering why a decision was selected, why alternatives were rejected,
 * supporting evidence, applied policies, confidence score, and utility breakdown.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionExplanation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String primaryRationale;
    private String selectedCandidateReason;

    @Builder.Default
    private Map<String, String> alternativeRejectionReasons = new ConcurrentHashMap<>();

    @Builder.Default
    private List<DecisionEvidence> supportingEvidence = Collections.emptyList();

    @Builder.Default
    private List<String> appliedPolicies = Collections.emptyList();

    private String confidenceSummary;

    @Builder.Default
    private Map<String, Double> utilityBreakdown = new ConcurrentHashMap<>();

    @Builder.Default
    private List<DecisionReason> decisionReasons = Collections.emptyList();
}
