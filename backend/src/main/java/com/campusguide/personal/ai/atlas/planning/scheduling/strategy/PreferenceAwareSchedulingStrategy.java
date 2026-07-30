package com.campusguide.personal.ai.atlas.planning.scheduling.strategy;

import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.scheduling.Schedule;
import com.campusguide.personal.ai.atlas.planning.scheduling.SchedulingStrategy;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Preference-aware scheduling strategy incorporating user start-time preferences.
 */
@Component
public class PreferenceAwareSchedulingStrategy implements SchedulingStrategy {

    private final EarliestCompletionSchedulingStrategy baseStrategy = new EarliestCompletionSchedulingStrategy();

    @Override
    public String getStrategyName() {
        return "PREFERENCE_AWARE";
    }

    @Override
    public Schedule schedule(TaskGraph taskGraph, PlanningContext context) {
        if (context != null && context.getSchedulingPreferences() != null && context.getSchedulingPreferences().getPreferredStartTime() != null) {
            Instant prefStart = context.getSchedulingPreferences().getPreferredStartTime();
            if (context.getTimeHorizon() != null) {
                context.getTimeHorizon().setStartTime(prefStart);
            }
        }

        Schedule schedule = baseStrategy.schedule(taskGraph, context);
        schedule.setStrategyUsed(getStrategyName());
        return schedule;
    }
}
