package com.campusguide.personal.ai.atlas.context.evidence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Multi-dimensional quality and relevance scoring model for evidence evaluation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenceScore {

    @Builder.Default
    private double relevanceScore = 0.5;

    @Builder.Default
    private double freshnessScore = 0.5;

    @Builder.Default
    private double confidenceScore = 0.5;

    @Builder.Default
    private double sourceAuthorityScore = 0.5;

    @Builder.Default
    private double qualityScore = 0.5;

    @Builder.Default
    private double overallScore = 0.5;

    /**
     * Calculates composite overall score based on standard weights.
     */
    public double calculateOverallScore() {
        this.overallScore = (relevanceScore * 0.30)
                + (confidenceScore * 0.25)
                + (sourceAuthorityScore * 0.20)
                + (freshnessScore * 0.15)
                + (qualityScore * 0.10);
        return this.overallScore;
    }
}
