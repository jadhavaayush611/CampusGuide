package com.campusguide.personal.ai.atlas.knowledge.graph.confidence;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Deterministic breakdown of factors contributing to graph reasoning confidence calculation.
 */
@Data
@Builder
public class ConfidenceFactors implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private double relationshipStrength = 0.8;

    @Builder.Default
    private double retrievalConfidence = 0.85;

    @Builder.Default
    private double evidenceQuality = 0.85;

    @Builder.Default
    private int traversalDepth = 1;

    @Builder.Default
    private double inferenceConfidence = 0.9;

    public static ConfidenceFactors defaults() {
        return ConfidenceFactors.builder().build();
    }
}
