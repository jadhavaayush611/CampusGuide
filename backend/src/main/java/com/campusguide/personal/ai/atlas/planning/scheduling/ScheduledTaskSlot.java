package com.campusguide.personal.ai.atlas.planning.scheduling;

import com.campusguide.personal.ai.atlas.planning.model.PlanningTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Scheduled slot for an individual PlanningTask.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledTaskSlot implements Serializable {

    private static final long serialVersionUID = 1L;

    private PlanningTask task;
    private Instant startTime;
    private Instant endTime;
    private double durationMinutes;
    private String assignedResource;
}
