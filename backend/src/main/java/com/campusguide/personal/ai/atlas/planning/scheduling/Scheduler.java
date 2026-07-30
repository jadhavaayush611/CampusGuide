package com.campusguide.personal.ai.atlas.planning.scheduling;

import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central Scheduler service coordinating scheduling strategy selection and schedule creation.
 */
@Slf4j
@Component("planningScheduler")
public class Scheduler {

    private final Map<String, SchedulingStrategy> strategyMap = new ConcurrentHashMap<>();

    public Scheduler(List<SchedulingStrategy> strategies) {
        if (strategies != null) {
            for (SchedulingStrategy st : strategies) {
                strategyMap.put(st.getStrategyName().toUpperCase(), st);
                log.debug("Registered scheduling strategy: {}", st.getStrategyName());
            }
        }
    }

    public Schedule schedule(TaskGraph taskGraph, PlanningContext context) {
        String preferredName = "EARLIEST_COMPLETION";
        if (context != null && context.getSchedulingPreferences() != null && context.getSchedulingPreferences().getStrategyPreference() != null) {
            preferredName = context.getSchedulingPreferences().getStrategyPreference().toUpperCase();
        }

        SchedulingStrategy strategy = strategyMap.get(preferredName);
        if (strategy == null) {
            log.warn("Preferred strategy {} not found. Falling back to EARLIEST_COMPLETION", preferredName);
            strategy = strategyMap.getOrDefault("EARLIEST_COMPLETION", strategyMap.values().stream().findFirst().orElseThrow());
        }

        log.debug("Executing scheduling strategy: {}", strategy.getStrategyName());
        return strategy.schedule(taskGraph, context);
    }
}
