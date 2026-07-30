package com.campusguide.personal.ai.atlas.planning.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * Boundaries and depth limits for goal decomposition and task graph generation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningScope implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private Set<String> allowedDomains = Collections.singleton("CAMPUS");

    @Builder.Default
    private int maxDecompositionDepth = 3;

    @Builder.Default
    private boolean allowOptionalTasks = true;

    @Builder.Default
    private boolean allowParallelExecution = true;

    public static PlanningScope defaultScope() {
        return PlanningScope.builder()
                .allowedDomains(Collections.singleton("CAMPUS"))
                .maxDecompositionDepth(3)
                .allowOptionalTasks(true)
                .allowParallelExecution(true)
                .build();
    }
}
