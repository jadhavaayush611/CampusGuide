package com.campusguide.personal.ai.atlas.planning.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Operational and domain constraints enforced during plan generation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningConstraints implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private long maxPlanningLatencyMs = 5000L;

    private Instant hardDeadline;

    @Builder.Default
    private int maxParallelTasks = 4;

    @Builder.Default
    private Set<String> restrictedActionTypes = Collections.emptySet();

    @Builder.Default
    private Set<String> requiredPermissions = Collections.emptySet();

    @Builder.Default
    private Map<String, Object> customConstraints = new ConcurrentHashMap<>();

    public static PlanningConstraints defaultConstraints() {
        return PlanningConstraints.builder()
                .maxPlanningLatencyMs(5000L)
                .maxParallelTasks(4)
                .restrictedActionTypes(Collections.emptySet())
                .requiredPermissions(Collections.emptySet())
                .build();
    }
}
