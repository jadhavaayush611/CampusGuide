package com.campusguide.personal.ai.atlas.knowledge.graph.confidence;

import org.springframework.stereotype.Component;

/**
 * Deterministic calculator for Graph Reasoning Confidence scores.
 */
@Component
public class ConfidenceCalculator {

    /**
     * Combines factors into a deterministic reasoning score.
     * Weights:
     * - Relationship Strength: 35%
     * - Retrieval Confidence: 25%
     * - Evidence Quality: 25%
     * - Inference Confidence: 15%
     * Traversal depth applies a depth penalty multiplier: 0.95^depth.
     */
    public ReasoningConfidence calculate(ConfidenceFactors factors) {
        if (factors == null) {
            factors = ConfidenceFactors.defaults();
        }

        double baseScore = (factors.getRelationshipStrength() * 0.35)
                + (factors.getRetrievalConfidence() * 0.25)
                + (factors.getEvidenceQuality() * 0.25)
                + (factors.getInferenceConfidence() * 0.15);

        double depthPenalty = Math.pow(0.95, Math.max(0, factors.getTraversalDepth() - 1));
        double finalScore = baseScore * depthPenalty;

        String explanation = String.format(
                "Score: %.2f (Base: %.2f, DepthPenalty: %.2f [depth=%d], RelStrength: %.2f, Retrieval: %.2f, Quality: %.2f, Inference: %.2f)",
                finalScore, baseScore, depthPenalty, factors.getTraversalDepth(),
                factors.getRelationshipStrength(), factors.getRetrievalConfidence(),
                factors.getEvidenceQuality(), factors.getInferenceConfidence()
        );

        return ReasoningConfidence.create(finalScore, factors, explanation);
    }
}
