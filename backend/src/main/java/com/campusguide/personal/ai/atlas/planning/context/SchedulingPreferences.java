package com.campusguide.personal.ai.atlas.planning.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Scheduling preferences provided in PlanningContext.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulingPreferences implements Serializable {

    private static final long serialVersionUID = 1L;

    private Instant preferredStartTime;
    private Instant deadline;

    @Builder.Default
    private int maxParallelTasks = 4;

    @Builder.Default
    private boolean allowOverlappingTasks = true;

    @Builder.Default
    private String strategyPreference = "EARLIEST_COMPLETION";

    public static SchedulingPreferences defaultPreferences() {
        return SchedulingPreferences.builder()
                .maxParallelTasks(4)
                .allowOverlappingTasks(true)
                .strategyPreference("EARLIEST_COMPLETION")
                .build();
    }
}
