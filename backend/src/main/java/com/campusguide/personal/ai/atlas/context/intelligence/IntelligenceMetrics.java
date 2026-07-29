package com.campusguide.personal.ai.atlas.context.intelligence;

import com.campusguide.personal.ai.atlas.context.fusion.ConflictResolution;
import com.campusguide.personal.ai.atlas.context.fusion.FusionDecision;
import com.campusguide.personal.ai.atlas.context.prioritization.PrioritizationDecision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Observability model capturing fusion decisions, conflict resolutions, evidence summaries,
 * cache metrics, prioritization ranks, and latency budget usage for Atlas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntelligenceMetrics {

    @Builder.Default
    private List<FusionDecision> fusionDecisions = new ArrayList<>();

    @Builder.Default
    private List<ConflictResolution> conflictResolutions = new ArrayList<>();

    @Builder.Default
    private List<PrioritizationDecision> prioritizationDecisions = new ArrayList<>();

    @Builder.Default
    private Map<String, String> evidenceSummaries = new LinkedHashMap<>();

    private long cacheHits;
    private long cacheMisses;
    private double cacheHitRatio;

    private long allocatedLatencyBudgetMs;
    private long usedLatencyBudgetMs;
    private boolean budgetExceeded;
    private boolean degradedMode;
}
