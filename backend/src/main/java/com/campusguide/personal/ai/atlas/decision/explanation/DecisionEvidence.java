package com.campusguide.personal.ai.atlas.decision.explanation;

import com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine.ReasoningEvidence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Supporting evidence container for decision explainability.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionEvidence implements Serializable {

    private static final long serialVersionUID = 1L;

    private String evidenceId;
    private String description;
    private double confidence;

    @Builder.Default
    private List<String> citedNodes = Collections.emptyList();

    @Builder.Default
    private List<String> citedRelationships = Collections.emptyList();

    private String source;

    public static DecisionEvidence fromReasoningEvidence(ReasoningEvidence reasoningEvidence) {
        if (reasoningEvidence == null) {
            return DecisionEvidence.builder()
                    .evidenceId("ev_empty")
                    .description("No reasoning evidence available")
                    .confidence(0.0)
                    .source("GraphReasoner")
                    .build();
        }

        return DecisionEvidence.builder()
                .evidenceId(reasoningEvidence.getEvidenceId())
                .description(reasoningEvidence.getReasoningSummaryText())
                .confidence(reasoningEvidence.getConfidence())
                .citedNodes(reasoningEvidence.getCitedNodeNames() != null ? reasoningEvidence.getCitedNodeNames() : Collections.emptyList())
                .citedRelationships(reasoningEvidence.getCitedRelationshipTypes() != null ? reasoningEvidence.getCitedRelationshipTypes() : Collections.emptyList())
                .source("GraphReasoner")
                .build();
    }
}
