package com.campusguide.personal.ai.atlas.planning.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Strategy configuration driving PlanningPipeline execution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningStrategy implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String strategyName = "DEFAULT";

    @Builder.Default
    private boolean enableOptimization = true;

    @Builder.Default
    private boolean enableConstraintSolving = true;

    @Builder.Default
    private boolean enableExplainability = true;

    public static PlanningStrategy defaultStrategy() {
        return PlanningStrategy.builder()
                .strategyName("DEFAULT")
                .enableOptimization(true)
                .enableConstraintSolving(true)
                .enableExplainability(true)
                .build();
    }

    public static PlanningStrategy fastStrategy() {
        return PlanningStrategy.builder()
                .strategyName("FAST")
                .enableOptimization(false)
                .enableConstraintSolving(true)
                .enableExplainability(false)
                .build();
    }

    public static PlanningStrategy thoroughStrategy() {
        return PlanningStrategy.builder()
                .strategyName("THOROUGH")
                .enableOptimization(true)
                .enableConstraintSolving(true)
                .enableExplainability(true)
                .build();
    }
}
