package com.campusguide.personal.ai.atlas.planning.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Objective for the Planning Engine.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningObjective implements Serializable {

    private static final long serialVersionUID = 1L;

    private String objectiveId;
    private String primaryGoal;
    private String targetDomain;

    @Builder.Default
    private int priority = 5;

    private String desiredOutcome;

    public static PlanningObjective defaultObjective(String goal) {
        return PlanningObjective.builder()
                .objectiveId("pobj_default")
                .primaryGoal(goal != null ? goal : "Execute plan for decision outcome")
                .targetDomain("CAMPUS")
                .priority(5)
                .desiredOutcome("Deterministic execution plan completed")
                .build();
    }
}
