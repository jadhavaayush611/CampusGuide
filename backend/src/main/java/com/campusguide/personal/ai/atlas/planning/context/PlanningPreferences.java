package com.campusguide.personal.ai.atlas.planning.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * User and system planning preferences driving optimizer and scheduling strategies.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningPreferences implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private String optimizationGoal = "COMPLETION_TIME"; // TIME, SIMPLICITY, RESOURCE, CONVENIENCE

    @Builder.Default
    private int maxTaskCount = 20;

    @Builder.Default
    private boolean includeOptionalTasks = true;

    @Builder.Default
    private double riskTolerance = 0.50;

    public static PlanningPreferences defaultPreferences() {
        return PlanningPreferences.builder()
                .optimizationGoal("COMPLETION_TIME")
                .maxTaskCount(20)
                .includeOptionalTasks(true)
                .riskTolerance(0.50)
                .build();
    }
}
