package com.campusguide.personal.ai.atlas.planning.scheduling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Schedule object containing scheduled task slots and duration info.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule implements Serializable {

    private static final long serialVersionUID = 1L;

    private String scheduleId;

    @Builder.Default
    private List<ScheduledTaskSlot> scheduledTasks = new ArrayList<>();

    private Instant startTime;
    private Instant endTime;
    private double totalDurationMinutes;
    private String strategyUsed;
    private boolean meetsDeadline;
}
