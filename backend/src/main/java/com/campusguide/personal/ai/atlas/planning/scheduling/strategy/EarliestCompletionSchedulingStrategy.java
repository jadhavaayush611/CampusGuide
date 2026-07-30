package com.campusguide.personal.ai.atlas.planning.scheduling.strategy;

import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskDependency;
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
 * Scheduling strategy minimizing total makespan by scheduling tasks as early as dependencies permit.
 */
@Component
public class EarliestCompletionSchedulingStrategy implements SchedulingStrategy {

    @Override
    public String getStrategyName() {
        return "EARLIEST_COMPLETION";
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

        List<PlanningTask> sortedTasks = taskGraph.hasCycle() ? taskGraph.getTaskList() : taskGraph.topologicalSort();
        Instant baseStart = context != null && context.getTimeHorizon() != null
                ? context.getTimeHorizon().getStartTime() : Instant.now();

        Map<String, Instant> finishTimes = new HashMap<>();
        List<ScheduledTaskSlot> slots = new ArrayList<>();

        for (PlanningTask task : sortedTasks) {
            Instant earliestTaskStart = baseStart;
            for (TaskDependency dep : taskGraph.getDependencies()) {
                if (dep.getSuccessorTaskId().equals(task.getTaskId())) {
                    Instant predFinish = finishTimes.get(dep.getPredecessorTaskId());
                    if (predFinish != null) {
                        long lag = (long) dep.getLagMinutes();
                        Instant candidate = predFinish.plus(lag, ChronoUnit.MINUTES);
                        if (candidate.isAfter(earliestTaskStart)) {
                            earliestTaskStart = candidate;
                        }
                    }
                }
            }

            long durationMinutes = Math.max(1L, (long) task.getEstimatedDurationMinutes());
            Instant taskEnd = earliestTaskStart.plus(durationMinutes, ChronoUnit.MINUTES);
            finishTimes.put(task.getTaskId(), taskEnd);

            ScheduledTaskSlot slot = ScheduledTaskSlot.builder()
                    .task(task)
                    .startTime(earliestTaskStart)
                    .endTime(taskEnd)
                    .durationMinutes(task.getEstimatedDurationMinutes())
                    .assignedResource("WORKER_THREAD")
                    .build();
            slots.add(slot);
        }

        Instant minStart = slots.stream().map(ScheduledTaskSlot::getStartTime).min(Instant::compareTo).orElse(baseStart);
        Instant maxEnd = slots.stream().map(ScheduledTaskSlot::getEndTime).max(Instant::compareTo).orElse(baseStart);
        long totalMinutes = ChronoUnit.MINUTES.between(minStart, maxEnd);

        boolean meetsDeadline = true;
        if (context != null && context.getConstraints() != null && context.getConstraints().getHardDeadline() != null) {
            meetsDeadline = !maxEnd.isAfter(context.getConstraints().getHardDeadline());
        }

        return Schedule.builder()
                .scheduleId("sch_" + UUID.randomUUID().toString().substring(0, 8))
                .scheduledTasks(slots)
                .startTime(minStart)
                .endTime(maxEnd)
                .totalDurationMinutes((double) totalMinutes)
                .strategyUsed(getStrategyName())
                .meetsDeadline(meetsDeadline)
                .build();
    }
}
