package com.campusguide.personal.ai.atlas.decision.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Objective of a decision task specifying intent, target domain, priority, and expected outcome type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionObjective implements Serializable {

    private static final long serialVersionUID = 1L;

    private String objectiveId;
    private String intent;
    private String primaryGoal;
    private String targetDomain;

    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Builder.Default
    private String expectedOutcomeType = "ACTION_RECOMMENDATION";

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public static DecisionObjective defaultObjective(String intent) {
        return DecisionObjective.builder()
                .objectiveId("obj_" + System.currentTimeMillis())
                .intent(intent != null ? intent : "general_query")
                .primaryGoal("Provide deterministic, optimized decision recommendation")
                .targetDomain("campus_guide")
                .priority(Priority.MEDIUM)
                .expectedOutcomeType("ACTION_RECOMMENDATION")
                .build();
    }
}
