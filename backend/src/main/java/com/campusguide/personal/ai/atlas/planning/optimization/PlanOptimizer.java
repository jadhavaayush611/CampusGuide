package com.campusguide.personal.ai.atlas.planning.optimization;

import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plan Optimizer coordinating completion time, dependency reduction, resource utilization,
 * user convenience, and plan simplicity optimization strategies.
 */
@Slf4j
@Component("planningPlanOptimizer")
public class PlanOptimizer {

    private final Map<String, OptimizationStrategy> strategyMap = new ConcurrentHashMap<>();

    public PlanOptimizer(List<OptimizationStrategy> strategies) {
        if (strategies != null) {
            for (OptimizationStrategy st : strategies) {
                strategyMap.put(st.getStrategyName().toUpperCase(), st);
                log.debug("Registered optimization strategy: {}", st.getStrategyName());
            }
        }
    }

    public OptimizationResult optimize(ExecutionPlan plan, TaskGraph graph, PlanningContext context) {
        if (plan == null) {
            return OptimizationResult.builder().build();
        }

        String targetGoal = "COMPLETION_TIME";
        if (context != null && context.getPlanningPreferences() != null && context.getPlanningPreferences().getOptimizationGoal() != null) {
            targetGoal = context.getPlanningPreferences().getOptimizationGoal().toUpperCase();
        }

        OptimizationStrategy strategy = strategyMap.get(targetGoal);
        if (strategy == null) {
            log.debug("Optimization goal {} not found, defaulting to COMPLETION_TIME", targetGoal);
            strategy = strategyMap.getOrDefault("COMPLETION_TIME", strategyMap.values().stream().findFirst().orElseThrow());
        }

        log.debug("Executing plan optimization strategy: {}", strategy.getStrategyName());
        return strategy.optimize(plan, graph, context);
    }
}
