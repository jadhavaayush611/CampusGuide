package com.campusguide.personal.ai.atlas.execution.runtime.monitoring;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Aggregated runtime statistics for workflow execution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalWorkflowsExecuted;
    private long successfulWorkflows;
    private long failedWorkflows;
    private long cancelledWorkflows;
    private double averageDurationMs;
    private long totalUnitsExecuted;
    private long totalRetries;
    private long totalRollbacks;

    public double getSuccessRate() {
        if (totalWorkflowsExecuted == 0) return 0.0;
        return (double) successfulWorkflows / totalWorkflowsExecuted;
    }
}
