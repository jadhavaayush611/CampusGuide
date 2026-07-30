package com.campusguide.personal.ai.atlas.execution.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Operational metrics captured during workflow execution preparation.
 * Guarantees zero sensitive payload or PII data is exposed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionPreparationMetrics implements Serializable {

    private static final long serialVersionUID = 1L;

    private long preparationLatencyMs;
    private long validationLatencyMs;
    private int totalStages;
    private int totalExecutionUnits;
    private int totalCheckpoints;
    private int validationFailureCount;
    private boolean approvalRequired;
    private String approvalLevel;
    private double overallRiskScore;
    private String riskCategory;
    private int rollbackStepCount;
    private double capabilityCoverageRatio;

    @Builder.Default
    private Map<String, Integer> unitsByType = new ConcurrentHashMap<>();
}
