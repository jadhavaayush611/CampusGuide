package com.campusguide.personal.ai.atlas.planning.scheduling.strategy;

import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.scheduling.Schedule;
import com.campusguide.personal.ai.atlas.planning.scheduling.SchedulingStrategy;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Deadline-aware scheduling strategy evaluating buffer margins against target deadlines.
 */
@Component
public class DeadlineAwareSchedulingStrategy implements SchedulingStrategy {

    private final EarliestCompletionSchedulingStrategy baseStrategy = new EarliestCompletionSchedulingStrategy();

    @Override
    public String getStrategyName() {
        return "DEADLINE_AWARE";
    }

    @Override
    public Schedule schedule(TaskGraph taskGraph, PlanningContext context) {
        Schedule baseSchedule = baseStrategy.schedule(taskGraph, context);
        baseSchedule.setStrategyUsed(getStrategyName());

        Instant deadline = null;
        if (context != null) {
            if (context.getConstraints() != null && context.getConstraints().getHardDeadline() != null) {
                deadline = context.getConstraints().getHardDeadline();
            } else if (context.getSchedulingPreferences() != null && context.getSchedulingPreferences().getDeadline() != null) {
                deadline = context.getSchedulingPreferences().getDeadline();
            }
        }

        if (deadline != null && baseSchedule.getEndTime() != null) {
            baseSchedule.setMeetsDeadline(!baseSchedule.getEndTime().isAfter(deadline));
        }

        return baseSchedule;
    }
}
