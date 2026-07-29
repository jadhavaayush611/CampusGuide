package com.campusguide.personal.ai.atlas.context.prioritization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Record of a context prioritization decision detailing calculated quality metrics and rank placement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrioritizationDecision {
    private String key;
    private String targetDomain;
    private double finalRankScore;
    private double relevance;
    private double freshness;
    private double confidence;
    private double completeness;
    private double evidenceStrength;
    private double sourceAuthority;
    private int rank;
    private boolean kept;
    private String reason;
}
