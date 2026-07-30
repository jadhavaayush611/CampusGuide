package com.campusguide.personal.ai.atlas.knowledge.graph.confidence;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Structured score and level indicating the deterministic confidence of a graph reasoning result.
 */
@Data
@Builder
public class ReasoningConfidence implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ConfidenceLevel {
        HIGH,
        MEDIUM,
        LOW,
        UNCERTAIN
    }

    private final double overallScore;
    private final ConfidenceLevel level;
    private final ConfidenceFactors factors;
    private final String explanation;

    public static ReasoningConfidence create(double score, ConfidenceFactors factors, String explanation) {
        double clampedScore = Math.max(0.0, Math.min(1.0, score));
        ConfidenceLevel lvl;
        if (clampedScore >= 0.8) {
            lvl = ConfidenceLevel.HIGH;
        } else if (clampedScore >= 0.5) {
            lvl = ConfidenceLevel.MEDIUM;
        } else if (clampedScore >= 0.3) {
            lvl = ConfidenceLevel.LOW;
        } else {
            lvl = ConfidenceLevel.UNCERTAIN;
        }

        return ReasoningConfidence.builder()
                .overallScore(clampedScore)
                .level(lvl)
                .factors(factors != null ? factors : ConfidenceFactors.defaults())
                .explanation(explanation != null ? explanation : "Deterministic confidence evaluation")
                .build();
    }
}
