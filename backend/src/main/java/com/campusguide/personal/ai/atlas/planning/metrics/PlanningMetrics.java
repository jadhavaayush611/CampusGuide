package com.campusguide.personal.ai.atlas.planning.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Operational metrics captured during plan generation.
 * Guarantees zero sensitive planning data is logged or stored.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningMetrics implements Serializable {

    private static final long serialVersionUID = 1L;

    private long planningLatencyMs;
    private long schedulingLatencyMs;
    private int totalTasks;
    private int totalDependencies;
    private int criticalPathLength;
    private double optimizationEffectiveness;
    private int constraintViolationCount;
    private double planComplexityScore;

    @Builder.Default
    private Map<String, Integer> taskCountsByState = new ConcurrentHashMap<>();
}
