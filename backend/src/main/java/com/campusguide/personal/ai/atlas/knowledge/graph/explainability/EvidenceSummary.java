package com.campusguide.personal.ai.atlas.knowledge.graph.explainability;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Summary metrics and narrative description of evidence gathered during graph reasoning.
 */
@Data
@Builder
public class EvidenceSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int totalNodesEvaluated;
    private final int totalEdgesTraversed;
    private final int totalInferencesApplied;
    private final double overallConfidence;
    private final String textualSummary;
}
