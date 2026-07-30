package com.campusguide.personal.ai.atlas.knowledge.graph.path;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * High-level logical chain of reasoning steps derived from EvidencePaths and InferenceResults.
 */
@Data
@Builder
public class ReasoningChain implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String chainId;

    @Builder.Default
    private final List<EvidencePath> evidencePaths = Collections.emptyList();

    @Builder.Default
    private final List<String> logicalSteps = Collections.emptyList();

    private final double overallChainConfidence;
    private final String rationale;

    public static ReasoningChain empty() {
        return ReasoningChain.builder()
                .chainId("empty_chain")
                .evidencePaths(Collections.emptyList())
                .logicalSteps(Collections.emptyList())
                .overallChainConfidence(0.0)
                .rationale("No reasoning path discovered")
                .build();
    }
}
