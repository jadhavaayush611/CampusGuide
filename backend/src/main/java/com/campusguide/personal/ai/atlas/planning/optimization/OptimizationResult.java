package com.campusguide.personal.ai.atlas.planning.optimization;

import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Result returned by PlanOptimizer containing the optimized plan and metric improvements.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private ExecutionPlan optimizedPlan;

    @Builder.Default
    private List<String> appliedOptimizations = new ArrayList<>();

    private double timeSavedMinutes;
    private int dependencyCountDelta;
    private double simplicityScoreDelta;
    private double overallImprovementRatio;
}
