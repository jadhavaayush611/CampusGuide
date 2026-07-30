package com.campusguide.personal.ai.atlas.planning.optimization.strategy;

import com.campusguide.personal.ai.atlas.planning.context.PlanningContext;
import com.campusguide.personal.ai.atlas.planning.graph.DependencyType;
import com.campusguide.personal.ai.atlas.planning.graph.TaskDependency;
import com.campusguide.personal.ai.atlas.planning.graph.TaskGraph;
import com.campusguide.personal.ai.atlas.planning.model.ExecutionPlan;
import com.campusguide.personal.ai.atlas.planning.model.PlanStatus;
import com.campusguide.personal.ai.atlas.planning.optimization.OptimizationResult;
import com.campusguide.personal.ai.atlas.planning.optimization.OptimizationStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Optimization strategy pruning redundant or soft dependencies.
 */
@Component
public class DependencyReductionOptimizationStrategy implements OptimizationStrategy {

    @Override
    public String getStrategyName() {
        return "DEPENDENCY_REDUCTION";
    }

    @Override
    public OptimizationResult optimize(ExecutionPlan plan, TaskGraph graph, PlanningContext context) {
        if (plan == null) {
            return OptimizationResult.builder().build();
        }

        int originalCount = plan.getDependencies() != null ? plan.getDependencies().size() : 0;
        List<TaskDependency> pruned = new ArrayList<>();
        if (plan.getDependencies() != null) {
            for (TaskDependency dep : plan.getDependencies()) {
                if (dep.getDependencyType() != DependencyType.SOFT) {
                    pruned.add(dep);
                }
            }
        }

        plan.setDependencies(pruned);
        plan.setStatus(PlanStatus.OPTIMIZED);

        int delta = originalCount - pruned.size();
        return OptimizationResult.builder()
                .optimizedPlan(plan)
                .appliedOptimizations(Collections.singletonList("PRUNED_SOFT_DEPENDENCIES"))
                .timeSavedMinutes(5.0 * delta)
                .dependencyCountDelta(-delta)
                .simplicityScoreDelta(0.15)
                .overallImprovementRatio(0.12)
                .build();
    }
}
