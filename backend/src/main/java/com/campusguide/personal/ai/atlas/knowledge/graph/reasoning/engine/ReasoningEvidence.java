package com.campusguide.personal.ai.atlas.knowledge.graph.reasoning.engine;

import com.campusguide.personal.ai.atlas.knowledge.graph.explainability.ReasoningExplanation;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * High-level evidence output from ReasoningEngine ready for integration into AtlasContext and Context Intelligence Layer.
 */
@Data
@Builder
public class ReasoningEvidence implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String evidenceId;
    private final String objectiveDescription;
    private final double confidence;
    private final String reasoningSummaryText;
    private final ReasoningExplanation explanation;

    @Builder.Default
    private final List<String> citedNodeNames = Collections.emptyList();

    @Builder.Default
    private final List<String> citedRelationshipTypes = Collections.emptyList();

    public static ReasoningEvidence empty() {
        return ReasoningEvidence.builder()
                .evidenceId("empty_reasoning_evidence")
                .objectiveDescription("No reasoning objective provided")
                .confidence(0.0)
                .reasoningSummaryText("No graph reasoning evidence discovered.")
                .explanation(null)
                .citedNodeNames(Collections.emptyList())
                .citedRelationshipTypes(Collections.emptyList())
                .build();
    }
}
