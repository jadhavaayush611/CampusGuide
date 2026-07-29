package com.campusguide.personal.ai.atlas.context.ranking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composite score details for a retrieved domain context item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContextScore implements Comparable<ContextScore> {

    private String contributorName;
    private double totalScore;

    private double intentRelevance;
    private double entityOverlap;
    private double freshness;
    private double sourcePriority;
    private double confidence;
    private double completeness;

    @Override
    public int compareTo(ContextScore other) {
        if (other == null) return -1;
        // Deterministic ordering: totalScore desc, sourcePriority desc, contributorName asc
        int comp = Double.compare(other.totalScore, this.totalScore);
        if (comp != 0) return comp;
        int prioComp = Double.compare(other.sourcePriority, this.sourcePriority);
        if (prioComp != 0) return prioComp;
        return this.contributorName.compareTo(other.contributorName);
    }
}
