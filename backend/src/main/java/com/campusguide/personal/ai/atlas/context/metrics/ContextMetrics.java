package com.campusguide.personal.ai.atlas.context.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Diagnostic metrics capturing execution stats for Atlas context build lifecycle,
 * semantic query understanding, intelligent retrieval strategies, and ranking scores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContextMetrics {

    @Builder.Default
    private Map<String, Long> executionTimeMs = new LinkedHashMap<>();

    @Builder.Default
    private List<String> skippedContributors = new ArrayList<>();

    @Builder.Default
    private Map<String, String> contributorFailures = new LinkedHashMap<>();

    private int estimatedContextSizeBytes;

    private int estimatedTokenCount;

    private long totalExecutionTimeMs;

    // --- Intelligent Retrieval & Query Diagnostics ---
    private String detectedIntent;
    private String normalizedQuery;

    @Builder.Default
    private List<String> extractedEntities = new ArrayList<>();

    @Builder.Default
    private List<String> executedStrategies = new ArrayList<>();

    @Builder.Default
    private List<String> skippedStrategies = new ArrayList<>();

    private long retrievalLatencyMs;

    @Builder.Default
    private Map<String, Double> relevanceScores = new LinkedHashMap<>();

    private double retrievalConfidence;

    // --- Intelligence & Fusion Diagnostics ---
    @Builder.Default
    private List<com.campusguide.personal.ai.atlas.context.fusion.FusionDecision> fusionDecisions = new ArrayList<>();

    @Builder.Default
    private List<com.campusguide.personal.ai.atlas.context.fusion.ConflictResolution> conflictResolutions = new ArrayList<>();

    @Builder.Default
    private List<com.campusguide.personal.ai.atlas.context.prioritization.PrioritizationDecision> prioritizationDecisions = new ArrayList<>();

    @Builder.Default
    private Map<String, String> evidenceSummaries = new LinkedHashMap<>();

    private long cacheHits;
    private long cacheMisses;
    private double cacheHitRatio;
    private long allocatedLatencyBudgetMs;
    private long usedLatencyBudgetMs;
    private boolean budgetExceeded;
}
