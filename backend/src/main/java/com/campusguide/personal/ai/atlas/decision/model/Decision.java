package com.campusguide.personal.ai.atlas.decision.model;

import com.campusguide.personal.ai.atlas.decision.context.DecisionObjective;
import com.campusguide.personal.ai.atlas.decision.explanation.DecisionEvidence;
import com.campusguide.personal.ai.atlas.decision.policy.PolicyComplianceResult;
import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Core Decision object encapsulating objectives, candidate set, selected action, confidence,
 * evidence, rationale, policy compliance, and metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Decision implements Serializable {

    private static final long serialVersionUID = 1L;

    private String decisionId;
    private DecisionObjective objective;
    
    @Builder.Default
    private List<DecisionCandidate> candidates = Collections.emptyList();

    private DecisionCandidate selectedCandidate;
    private double confidence;

    private ReasoningEvidence reasoningEvidence;

    @Builder.Default
    private List<DecisionEvidence> decisionEvidence = Collections.emptyList();

    private String rationale;
    private PolicyComplianceResult policyCompliance;

    @Builder.Default
    private DecisionMetadata metadata = new DecisionMetadata();

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant evaluatedAt = Instant.now();
}
