package com.campusguide.personal.ai.atlas.planning.scheduling.strategy;

import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.model.PlanningTask;
import com.campusguide.personal.ai.atlas.planning.scheduling.Schedule;
import com.campusguide.personal.ai.atlas.planning.scheduling.ScheduledTaskSlot;
import com.campusguide.personal.ai.atlas.planning.scheduling.SchedulingStrategy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Priority-aware scheduling strategy prioritizing high-priority tasks in earlier slots.
 */
@Component
public class PriorityAwareSchedulingStrategy implements SchedulingStrategy {

    @Override
    public String getStrategyName() {
        return "PRIORITY_AWARE";
    }

    @Override
    public Schedule schedule(TaskGraph taskGraph, PlanningContext context) {
        if (taskGraph == null || taskGraph.getTasks().isEmpty()) {
            Instant now = Instant.now();
            return Schedule.builder()
                    .scheduleId("sch_" + UUID.randomUUID().toString().substring(0, 8))
                    .startTime(now)
                    .endTime(now)
                    .totalDurationMinutes(0.0)
                    .strategyUsed(getStrategyName())
                    .meetsDeadline(true)
                    .build();
        }

        // Sort topologically, then sort ready batches by mandatory / duration priority
        List<PlanningTask> tasks = new ArrayList<>(taskGraph.hasCycle() ? taskGraph.getTaskList() : taskGraph.topologicalSort());
        tasks.sort((t1, t2) -> {
            if (t1.isMandatory() != t2.isMandatory()) {
                return t1.isMandatory() ? -1 : 1; // Mandatory first
            }
            return Double.compare(t2.getEstimatedDurationMinutes(), t1.getEstimatedDurationMinutes());
        });

        Instant baseStart = context != null && context.getTimeHorizon() != null
                ? context.getTimeHorizon().getStartTime() : Instant.now();

        List<ScheduledTaskSlot> slots = new ArrayList<>();
        Instant cursor = baseStart;

        for (PlanningTask task : tasks) {
            long durationMinutes = Math.max(1L, (long) task.getEstimatedDurationMinutes());
            Instant end = cursor.plus(durationMinutes, ChronoUnit.MINUTES);

            ScheduledTaskSlot slot = ScheduledTaskSlot.builder()
                    .task(task)
                    .startTime(cursor)
                    .endTime(end)
                    .durationMinutes(task.getEstimatedDurationMinutes())
                    .assignedResource("HIGH_PRIORITY_RESOURCE")
                    .build();
            slots.add(slot);
            cursor = end;
        }

        Instant minStart = slots.get(0).getStartTime();
        Instant maxEnd = slots.get(slots.size() - 1).getEndTime();
        long totalMinutes = ChronoUnit.MINUTES.between(minStart, maxEnd);

        return Schedule.builder()
                .scheduleId("sch_" + UUID.randomUUID().toString().substring(0, 8))
                .scheduledTasks(slots)
                .startTime(minStart)
                .endTime(maxEnd)
                .totalDurationMinutes((double) totalMinutes)
                .strategyUsed(getStrategyName())
                .meetsDeadline(true)
                .build();
    }
}
