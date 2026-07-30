package com.campusguide.personal.ai.atlas.knowledge.graph.explainability;

import com.campusguide.personal.ai.atlas.knowledge.graph.confidence.ReasoningConfidence;
import com.campusguide.personal.ai.atlas.knowledge.graph.path.EvidencePath;
import com.campusguide.personal.ai.atlas.knowledge.graph.path.ReasoningChain;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Complete structured explanation of a graph reasoning operation result.
 */
@Data
@Builder
public class ReasoningExplanation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String explanationId;
    private final ReasoningChain reasoningChain;
    private final EvidencePath primaryEvidencePath;
    private final ReasoningConfidence confidence;

    @Builder.Default
    private final List<String> assumptions = Collections.emptyList();

    @Builder.Default
    private final List<String> citedArtifacts = Collections.emptyList();

    @Builder.Default
    private final List<String> citedGraphEdges = Collections.emptyList();

    @Builder.Default
    private final List<ExplanationStep> steps = Collections.emptyList();

    private final EvidenceSummary summary;
}
