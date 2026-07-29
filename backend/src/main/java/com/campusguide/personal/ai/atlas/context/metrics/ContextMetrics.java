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
 * Diagnostic metrics capturing execution stats for Atlas context build lifecycle.
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
}
